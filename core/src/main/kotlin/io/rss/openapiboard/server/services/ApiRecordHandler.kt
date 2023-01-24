package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.Namespace
import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.services.accesscontrol.AssertRequiredAuthorities
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.support.NotificationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

        @Value("\${namespace.auto-create:false}")
        private val autoCreateNamespace: Boolean = false
) {

    @Transactional(readOnly = false)
    @PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
    @AssertRequiredAuthorities
    fun createOrUpdate(@Valid apiRecord: ApiRecord): ApiRecord {
        validateRecordFromRequest(apiRecord)

        apiRecord.version = apiRecord.version.ifBlank { DEFAULT_VERSION }

        val existingRecord = repository.findByNamespaceName(apiRecord.namespace, apiRecord.name)

        LOGGER.info("Storing API record: [${apiRecord.name}] in namespace [${apiRecord.namespace}]")
        val result = existingRecord
                ?.let {updateExistingRecord(it, apiRecord) }
                ?: repository.saveAndFlush(apiRecord)

        snapshotService.create(result)
        notificationHandler.notifyUpdate(result)
        doSourceProcessing(result)

        return result
    }

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
            LOGGER.info("Namespace '$namespace' doesn't exists, but auto-create option is enabled. Will be created.")
            namespaceHandler.saveNamespace(Namespace(namespace), listOf())
            return
        }

        throw BoardApplicationException("Fail when creating API record: Namespace '$namespace' does not exist")
    }

    private fun doSourceProcessing(result: ApiRecord) = apiSourceProcessor.processApiRecordAsync(result)

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