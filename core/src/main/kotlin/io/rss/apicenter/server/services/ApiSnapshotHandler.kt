package io.rss.apicenter.server.services

import io.rss.apicenter.server.helper.assertRequired
import io.rss.apicenter.server.persistence.dao.ApiRecordRepository
import io.rss.apicenter.server.persistence.dao.ApiSnapshotRepository
import io.rss.apicenter.server.persistence.entities.ApiRecord
import io.rss.apicenter.server.persistence.entities.ApiSnapshot
import io.rss.apicenter.server.persistence.entities.ApiSnapshotId
import io.rss.apicenter.server.security.Roles
import io.rss.apicenter.server.services.exceptions.BoardApplicationException
import io.rss.apicenter.server.services.to.ApiComparisonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.validation.Valid

/** Provides CRUD operations for ApiSnapshot, making use of Async in some cases */

@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
@Validated
class ApiSnapshotHandler (
        private val snapshotRepository: ApiSnapshotRepository,
        private val apiRecordRepository: ApiRecordRepository
) {

    /**
     * Stores a new Snapshot, <b>Async</b>
     */
    @Async("threadPoolTaskExecutor")
    fun create(@Valid api: ApiRecord) {
        val snap = ApiSnapshot(api.name, api.namespace, api.version).apply {
            this.source = api.source
            this.apiUrl = api.apiUrl
        }
        snapshotRepository.save(snap)

        LOGGER.info("${api.name} registered")
    }

    /** Delegates searching of version list for given Api/namespace */
    fun listVersionsByApi(apiId: UUID): List<String> {
        assertRequired(apiId) {"Api id is required for this query"}

        val api = apiRecordRepository.findApiNamespace(apiId)
                ?: return listOf()

        return snapshotRepository.findApiVersionList(api.apiName, api.namespace)
    }

    /** Search given snapshots and creates a comparison with them.
     * @throws BoardApplicationException if some of given Apis is not found
     * */
    fun buildComparison(sourceApi: ApiSnapshotId, targetApi: ApiSnapshotId): ApiComparisonResponse {
        val sourceResult = snapshotRepository.findById(sourceApi)
        val comparedResult = snapshotRepository.findById(targetApi)

        if (!sourceResult.isPresent || !comparedResult.isPresent) {
            throw BoardApplicationException("Comparison not possible. One or both of specified Api was not found")
        }

        return ApiComparisonResponse(sourceResult.get(), comparedResult.get())
    }

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ApiSnapshotHandler::class.java)
    }
}