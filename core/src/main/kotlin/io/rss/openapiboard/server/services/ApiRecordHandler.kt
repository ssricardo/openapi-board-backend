package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import io.rss.openapiboard.server.services.support.NotificationHandler
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.Resource

/**
 * Offers general CRUD operations for ApiRecord
 *
 * @author ricardo saturnino
 */
@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.READER}')")
@Transactional(readOnly = true)
class ApiRecordHandler {

    @Resource
    lateinit var repository: ApiRecordRepository

    @Resource
    lateinit var snapshotService: ApiSnapshotHandler

    @Resource
    lateinit var apiSourceProcessor: ApiSourceProcessor

    @Resource
    lateinit var notificationHandler: NotificationHandler

    @Transactional
    @PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
    fun createOrUpdate(apiRecord: ApiRecord): ApiRecord {
        assertStringRequired(apiRecord.name) {"Name must not be null"}
        assertStringRequired(apiRecord.namespace) {"Namespace must not be null"}
        assertStringRequired(apiRecord.source) {"Api specification must have some value"}
        assertStringRequired(apiRecord.apiUrl) {"App address must have some value"}

        if (apiRecord.version.isNullOrBlank()) {
            apiRecord.version = DEFAULT_VERSION
        }

        val result = repository.saveAndFlush(apiRecord).also {
            snapshotService.create(it)
            notificationHandler.notifyUpdate(it)
        }

        doSourceProcessing(result)
        return result
    }

    private fun doSourceProcessing(result: ApiRecord) = apiSourceProcessor.processApiRecord(result)

    /** Gets all namespaces, delegating retrieval operation */
    fun listNamespaces(): List<String> = repository.findAllNamespace()

    /** Retrieves list of "app with its version" matching given namespace */
    fun listApiByNamespace(nm: String) = repository.findApiListByNamespace(nm)

    /** Finds the ApiRecord related to given parameter and loads it with field "source" */
    fun loadApiRecord(id: ApiRecordId): ApiRecord? {
        return repository.findById(id)
                .map(apiSourceProcessor::enrichApiRecordSource)
                .orElse(null)
    }

    fun loadApiSource(id: ApiRecordId): String? {
        return loadApiRecord(id)?.source
    }

    private companion object {
        const val DEFAULT_VERSION = "No-Version"
    }

}