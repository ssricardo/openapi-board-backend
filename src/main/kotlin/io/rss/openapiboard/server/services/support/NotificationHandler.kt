package io.rss.openapiboard.server.services.support

import io.rss.openapiboard.server.config.EnvironmentConfig
import io.rss.openapiboard.server.config.PATH_UNSUBSCRIBE
import io.rss.openapiboard.server.helper.TokenHelper
import io.rss.openapiboard.server.helper.assertRequired
import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import io.rss.openapiboard.server.persistence.dao.AppSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.services.to.SubscriptionMailId
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.mail.Message
import javax.mail.internet.InternetAddress

/** Responsible for notify the subscribers according to its needs */
@Service
@PreAuthorize("hasAuthority('${Roles.MANAGER}')")
class NotificationHandler() {

    companion object {
        const val NOTIFICATION_MAIL_SUBJECT: String = "[OaBoard Notification] An application that you follow was updated"
    }

    @Inject
    private lateinit var appSnapshotRepository: AppSnapshotRepository

    @Inject
    private lateinit var subscriptionRepository: AlertSubscriptionRepository

    @Inject
    private lateinit var executorService: ExecutorService

    @Inject
    private lateinit var envConfig: EnvironmentConfig

    @Inject
    private lateinit var emailSender: JavaMailSender

    @PostConstruct
    internal fun init() {
    }

    @Async
    fun notifyUpdate(appRecord: AppRecord) {
        if (! envConfig.mailNotificationEnabled) {
            return
        }
        assertRequired(appRecord.name){"Invalid AppRecord given. Name is mandatory"}

        getAppChangeSpec(appRecord)?.let { change ->
            getListOfSubscribers(appRecord.name ?: throw IllegalStateException())
                    .filter { subs -> isSubscriptionMatchDiff(change, subs) }
                    .forEach { subs -> submitNotification(change, subs) }

        }
    }

    private fun getAppChangeSpec(appRecord: AppRecord): AppChange? {
        val previous = appSnapshotRepository.findTopPreviousVersion(appRecord.name!!,
                envConfig.mainNamespace, appRecord.version!!)

        // simplified first version. Idea for later: parse the definitions, find specific paths changed

        val areSame =  previous?.source?.equals(appRecord.source)
                ?: return AppChange(appRecord, "NEW")
        return if (areSame) null else AppChange(appRecord, "NOT_YET_DEFINED")
    }

    /** Does the subscription filters match what was changed? */
    private fun isSubscriptionMatchDiff(change: AppChange, subs: AlertSubscription): Boolean {
        return true // for this first version, filtering is not yet supported. TODO later
    }

    private fun submitNotification(change: AppChange, subs: AlertSubscription) {
        val unsubscribeLink = createUnsubsLink(change.app.name!!, subs.email!!)
        val mailContent = NotificationTemplate(change.app.lastModified,
                appName = change.app.name!!, newVersion = change.app.version!!,
                unsubscribeLink = unsubscribeLink)

        executorService.submit {
            sendMail(subs.email!!, mailContent)
        }
    }

    private fun createUnsubsLink(appName: String, email: String): String {
        val token = createToken(appName, email)
        return "${envConfig.serverAddress}$PATH_UNSUBSCRIBE$token"
    }

    private fun sendMail(emailAddress: String, mailContent: String) {
        val message = emailSender.createMimeMessage().apply {
            setSubject(NOTIFICATION_MAIL_SUBJECT)
        }

        val h = MimeMessageHelper(message, true, StandardCharsets.UTF_8.name()).apply {
            setFrom("oaBoard@noreply")
            setBcc(emailAddress)
            setText(mailContent, true)
        }

        emailSender.send(h.mimeMessage)
    }

    private fun getListOfSubscribers(appName: String) =
            subscriptionRepository.findByApp(appName)

    private fun createToken(appId: String, email: String): String =
            TokenHelper.generateMailToken(SubscriptionMailId(appId, email))

    /** Holds the diff and kind of diff: path removed? changed?... */
    data class AppChange(val app: AppRecord, val kind: String? = null)

}