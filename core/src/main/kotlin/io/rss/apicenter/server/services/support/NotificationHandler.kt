package io.rss.apicenter.server.services.support

import com.fasterxml.jackson.core.util.JacksonFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.rss.apicenter.server.config.EnvironmentConfig
import io.rss.apicenter.server.helper.assertGetStringsRequired
import io.rss.apicenter.server.helper.assertRequired
import io.rss.apicenter.server.persistence.dao.ApiSubscriptionRepository
import io.rss.apicenter.server.persistence.dao.ApiSnapshotRepository
import io.rss.apicenter.server.persistence.entities.ApiSubscription
import io.rss.apicenter.server.persistence.entities.ApiRecord
import io.rss.apicenter.server.security.Roles
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import javax.annotation.PostConstruct
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

/** Responsible for notify the subscribers according to its needs */
@Service
@PreAuthorize("hasAuthority('${Roles.MANAGER}')")
class NotificationHandler (
    private val apiSnapshotRepository: ApiSnapshotRepository,
    private val subscriptionRepository: ApiSubscriptionRepository,
    private val executorService: ExecutorService,
    private val envConfig: EnvironmentConfig,
    private val restClient: Client = DEFAULT_REST_CLIENT
) {

//    @PostConstruct
//    internal fun init() {
//        restClient = ClientBuilder.newBuilder()
//            .register(JacksonFeature::class)
//            .register(JavaTimeModule::class)
//            .build()
//    }

    @Async("threadPoolTaskExecutor")
    fun notifyUpdate(apiRecord: ApiRecord) {
        if (!envConfig.hooksNotificationEnabled) {
            return
        }
        assertRequired(apiRecord.name){"Invalid ApiRecord given. Name is mandatory"}

        LOGGER.info("Notify API change for ${apiRecord.name} in ${apiRecord.namespace}")
        val change = getApiChangeSpec(apiRecord)

        getListOfSubscribers(apiRecord.name)
                .filter { subs -> isSubscriptionMatchDiff(change, subs) }
                .forEach { subs -> handleNotification(change, subs) }
    }

    private fun getApiChangeSpec(apiRecord: ApiRecord): ApiChange {
        val previous = apiSnapshotRepository.findTopPreviousVersion(apiRecord.name,
                apiRecord.namespace, apiRecord.version, PageRequest.of(0, 1))
                .firstOrNull()
            ?: return ApiChange(apiRecord, ChangeType.NEW_API)

        // simplified first version, doesn't filter paths yet. Idea for later: parse the definitions, find specific paths changed

        val areSame =  previous.source?.equals(apiRecord.source) ?: false

        return if (areSame)
            ApiChange(apiRecord, ChangeType.NO_CHANGE, previous.version)
        else
            ApiChange(apiRecord, ChangeType.SOURCE_DIFF, previous.version)
    }

    /** Does the subscription filters match what was changed? */
    private fun isSubscriptionMatchDiff(change: ApiChange, subs: ApiSubscription): Boolean {
        if (subs.onlyOnChange && change.type == ChangeType.NO_CHANGE) {
            return false
        }

        val shouldFilterMatchingNamespace = subs.namepace != null
        if (shouldFilterMatchingNamespace && !subs.namepace.equals(change.api.namespace)) {
            return false
        }

        return true
    }

    private fun handleNotification(change: ApiChange, subscription: ApiSubscription) {
        try {
            submitNotification(change, subscription)
        } catch (e: Exception) {
            LOGGER.warn("""Error on submitting a notification: ${e.message}. 
                |Subscription ${subscription.id} (${subscription.targetWebhook}) will be ignored for ${change.api.name}
                |""".trimMargin())
        }
    }

    private fun submitNotification(change: ApiChange, subscription: ApiSubscription) {
        val (email) = assertGetStringsRequired({ "Missing webhook spec on subscription ${subscription.id}" },
            subscription.targetWebhook)

        val hookContent = NotificationHookData(
            apiName = change.api.name,
            namespace = change.api.namespace,
            newVersion = change.api.version,
            changeDate = change.api.modifiedDate,
            oldVersion = change.previousVersion
        )

        executorService.submit {
            sendMail(email, hookContent)
        }
    }

    private fun sendMail(targetHook: String, content: NotificationHookData) {
        val response = restClient.target(targetHook)
            .request()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .post(Entity.json(content))

        if (response.status < 300) {
            LOGGER.debug("Notification sent to $targetHook, for ${content.apiName}")
        }
    }

    private fun getListOfSubscribers(appName: String) =
            subscriptionRepository.findByApi(appName)

    /** Holds the diff and kind of diff: path removed? changed?... */
    private data class ApiChange(val api: ApiRecord, val type: ChangeType, val previousVersion: String? = null)

    private enum class ChangeType {
        NEW_API,
        SOURCE_DIFF,
        PATH_DIFF,
        NO_CHANGE
    }

    private companion object {
        val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")
        val LOGGER: Logger = LoggerFactory.getLogger(NotificationHandler::class.java)

        val DEFAULT_REST_CLIENT: Client by lazy {
            ClientBuilder.newBuilder()
                .register(JacksonFeature::class)
                .register(JavaTimeModule::class)
                .build()
        }
    }
}