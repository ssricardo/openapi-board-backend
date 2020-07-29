package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.dao.AppRecordRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.services.support.NotificationHandler
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.transaction.Transactional

/**
 * Service on business layer for AppRecord, offers related CRUD operations
 *
 * @author ricardo saturnino
 */
@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.READER}')")
class AppRecordHandler {

    private companion object {
        const val DEFAULT_VERSION = "No-Version"
    }

    @Inject
    lateinit var repository: AppRecordRepository

    @Inject
    lateinit var snapshotService: AppSnapshotHandler

    @Inject
    lateinit var appSourceProcessor: AppSourceProcessor

    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Transactional
    @PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
    fun createOrUpdate(appRecord: AppRecord): AppRecord {
        assertStringRequired(appRecord.name) {"Name must not be null"}
        assertStringRequired(appRecord.namespace) {"Namespace must not be null"}
        assertStringRequired(appRecord.source) {"Api specification must have some value"}
        assertStringRequired(appRecord.address) {"App address must have some value"}

        if (appRecord.version.isNullOrBlank()) {
            appRecord.version = DEFAULT_VERSION
        }

        val result = repository.saveAndFlush(appRecord).also {
            snapshotService.feed(it)
            notificationHandler.notifyUpdate(it)
        }

        doSourceProcessing(result)
        return result
    }

    private fun doSourceProcessing(result: AppRecord) = appSourceProcessor.processAppRecord(result)

    /** Gets all namespaces, delegating retrieval operation */
    fun listNamespaces(): List<String> = repository.findAllNamespace()

    /** Retrieves list of "app with its version" matching given namespace */
    fun listAppsByNamespace(nm: String) = repository.findAppsByNamespace(nm)

    /** Finds the AppRecord related to given parameter and loads it with field "source" */
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun loadAppRecord(id: AppRecordId): AppRecord? {
        return repository.findById(id)
                .map(appSourceProcessor::enrichAppRecordSource)
                .orElse(null)
    }

}