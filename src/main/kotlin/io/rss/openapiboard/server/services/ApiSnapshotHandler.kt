package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.dao.ApiSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import io.rss.openapiboard.server.persistence.entities.ApiSnapshot
import io.rss.openapiboard.server.persistence.entities.ApiSnapshotId
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.ApiComparisonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.inject.Inject

/** Provides CRUD operations for ApiSnapshot, making use of Async in some cases */

@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
class ApiSnapshotHandler {

    @Inject
    lateinit var repository: ApiSnapshotRepository

    /**
     * Stores a new Snapshot, <b>Async</b>
     */
    @Async
    fun create(api: ApiRecord) {
        assert(api.name != null && api.namespace != null)
        assert(api.version != null) {
            "Version is required when registering an Api. It's not possible to save it's history for ${api.name}"}

        val snap = ApiSnapshot(api.name, api.namespace, api.version).apply {
            this.source = api.source
            this.apiUrl = api.apiUrl
        }
        repository.save(snap)

        LOGGER.info("${api.name} registered")
    }

    /** Delegates searching of version list for given Api/namespace */
    fun listVersionsByApiNamespace(api: String, namespace: String): List<String> {
        assertStringRequired(api) {"Api name is required for this query"}
        assertStringRequired(namespace) {"Namespace is required for this query"}
        return repository.findApiVersionList(ApiRecordId(api, namespace))
    }

    /** Search given snapshots and creates a comparison with them.
     * @throws BoardApplicationException if some of given Apis is not found
     * */
    fun createComparison(sourceApi: ApiSnapshotId, targetApi: ApiSnapshotId): ApiComparisonResponse {
        val sourceResult = repository.findById(sourceApi)
        val comparedResult = repository.findById(targetApi)

        if (!sourceResult.isPresent || !comparedResult.isPresent) {
            throw BoardApplicationException("Comparision not possible. One or both of specified Api was not found")
        }

        return ApiComparisonResponse(sourceResult.get(), comparedResult.get())
    }

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ApiSnapshotHandler::class.java)
    }
}