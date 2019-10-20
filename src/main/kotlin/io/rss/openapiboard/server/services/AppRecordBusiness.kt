package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.AppRecordRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import org.springframework.stereotype.Service
import org.springframework.util.Assert
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
    lateinit var snapshotService: AppSnapshotService

    fun createOrUpdate(appRecord: AppRecord): AppRecord {
        Assert.state(appRecord.name != null) {"Name must not be null"}
        Assert.state(appRecord.namespace != null) {"Namespace must not be null"}
        Assert.state(! appRecord.source.isNullOrBlank()) {"Api specification must have some value"}
        Assert.state(! appRecord.address.isNullOrBlank()) {"App address must have some value"}

        if (appRecord.source.isNullOrBlank()) {
            appRecord.source = DEFAULT_VERSION
        }

        return repository.save(appRecord).also {
            snapshotService.feed(it)
        }
    }

    fun listNamespaces(): List<String> = repository.findAllNamespace()

    /** Retrieves list of AppRecords matching given namespace */
    fun listNamesByNamespace(nm: String): List<String> =
        repository.findNamesByNamespace(nm)


    /** Finds the AppRecord related to given parameter and loads it with field "source" */
    fun loadAppRecord(id: AppRecordId): AppRecord? =
        repository.getOne(id)

}