package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.AppVersionDto
import io.rss.openapiboard.server.persistence.dao.AppRecordRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import org.springframework.stereotype.Service
import javax.inject.Inject

/**
 * Service on business layer for AppRecord
 *
 * @author ricardo saturnino
 */
@Service
class AppRecordBusiness {

    private companion object {
        const val DEFAULT_VERSION = "No-Version"
    }

    @Inject
    lateinit var repository: AppRecordRepository

    @Inject
    lateinit var snapshotService: AppSnapshotBusiness

    fun createOrUpdate(appRecord: AppRecord): AppRecord {
        assertStringRequired(appRecord.name) {"Name must not be null"}
        assertStringRequired(appRecord.namespace) {"Namespace must not be null"}
        assertStringRequired(appRecord.source) {"Api specification must have some value"}
        assertStringRequired(appRecord.address) {"App address must have some value"}

        if (appRecord.version.isNullOrBlank()) {
            appRecord.version = DEFAULT_VERSION
        }

        return repository.save(appRecord).also {
            snapshotService.feed(it)
        }
    }

    /** Gets all namespaces, delegating retrieval operation */
    fun listNamespaces(): List<String> = repository.findAllNamespace()

    /** Retrieves list of "app with its version" matching given namespace */
    fun listAppsByNamespace(nm: String): List<AppVersionDto> =
        repository.findAppsByNamespace(nm)


    /** Finds the AppRecord related to given parameter and loads it with field "source" */
    fun loadAppRecord(id: AppRecordId): AppRecord? =
        repository.getOne(id)

}