package io.rss.apicenter.server.services

import io.rss.apicenter.server.helper.assertStringRequired
import io.rss.apicenter.server.persistence.dao.ApiRecordRepository
import io.rss.apicenter.server.persistence.entities.ApiRecord
import io.rss.apicenter.server.persistence.entities.Namespace
import io.rss.apicenter.server.security.Roles
import io.rss.apicenter.server.services.accesscontrol.AssertRequiredAuthorities
import io.rss.apicenter.server.services.exceptions.BoardApplicationException
import io.rss.apicenter.server.services.support.NotificationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.validation.Valid

@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.READER}')")
@Transactional(readOnly = true)
@Validated
class ApiRecordHandler (
        private val repository: ApiRecordRepository,
        private val namespaceHandler: NamespaceHandler,
        private val snapshotService: ApiSnapshotHandler,
        private val apiSourceProcessor: ApiSourceProcessor,
        private val notificationHandler: NotificationHandler,

        @Qualifier("requiresNew")
        private val transactionTemplate: TransactionTemplate,

        @Value("\${env.namespace.auto-create:false}")
        private val autoCreateNamespace: Boolean = false
) {

    @Transactional(readOnly = false)
    @PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
    @AssertRequiredAuthorities
    fun createOrUpdate(@Valid apiRecord: ApiRecord): ApiRecord {
        validateRecordFromRequest(apiRecord)

        apiRecord.version = apiRecord.version.ifBlank { DEFAULT_VERSION }

        val result = storeApiAndSnapshotInNewTransaction(apiRecord)

        notificationHandler.notifyUpdateAsync(result)
        apiSourceProcessor.processApiRecordAsync(result)

        return result
    }

    private fun storeApiAndSnapshotInNewTransaction(apiRecord: ApiRecord) =
            transactionTemplate.execute {
                val existingRecord = repository.findByNamespaceName(apiRecord.namespace, apiRecord.name)

                LOGGER.info("Storing API record: [${apiRecord.name}] in namespace [${apiRecord.namespace}]")
                val savedRecord = existingRecord
                        ?.let { updateExistingRecord(it, apiRecord) }
                        ?: repository.saveAndFlush(apiRecord)

                snapshotService.create(savedRecord)

                savedRecord
            }!!

    private fun validateRecordFromRequest(apiRecord: ApiRecord) {
        assertStringRequired(apiRecord.source) { "Api specification must have some value" }
        assertStringRequired(apiRecord.apiUrl) { "Api address must have some value" }
        assertNamespaceExists(apiRecord.namespace)
    }

    private fun updateExistingRecord(dbRecord: ApiRecord, requestRecord: ApiRecord): ApiRecord {
        with(requestRecord) {
            dbRecord.version = version
            dbRecord.apiUrl = apiUrl
            dbRecord.basePath = basePath
            dbRecord.source = source
            return repository.saveAndFlush(dbRecord)
        }
    }

    private fun assertNamespaceExists(namespace: String) {
        if (namespaceHandler.exists(namespace)) {
            return
        }

        if (autoCreateNamespace) {
            LOGGER.info("Namespace '$namespace' doesn't exist, but auto-create option is enabled. Will be created.")
            namespaceHandler.saveNamespace(Namespace(namespace), listOf())
            return
        }

        throw BoardApplicationException("Fail when creating API record: Namespace '$namespace' does not exist")
    }

    /** Retrieves list of "app with its version" matching given namespace */
    @AssertRequiredAuthorities
    fun listApiByNamespace(nm: Namespace) = repository.findApiVersionByNamespace(nm.name)

    /** Finds the ApiRecord related to given parameter and loads it with field "source" */
    @AssertRequiredAuthorities
    fun loadApiRecord(id: UUID): ApiRecord? {
        return repository.findByIdOrNull(id)
                ?.let(apiSourceProcessor::enrichApiRecordSource)
    }

    fun loadApiSource(id: UUID): String? {
        return loadApiRecord(id)?.source
    }

    private companion object {
        const val DEFAULT_VERSION = "No-Version"
        val LOGGER: Logger = LoggerFactory.getLogger(ApiRecordHandler::class.java)
    }

}