package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import io.rss.openapiboard.server.persistence.entities.ApiSnapshot
import io.rss.openapiboard.server.persistence.entities.ApiSnapshotId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApiSnapshotRepository: PagingAndSortingRepository<ApiSnapshot, ApiSnapshotId> {

    @Query("""
       SELECT DISTINCT a.version
       FROM ApiSnapshot a
       WHERE a.name = :#{#api.name} AND a.namespace = :#{#api.namespace}
    """)
    fun findApiVersionList(@Param("api") appId: ApiRecordId): List<String>

    @Query("""
        SELECT a 
        FROM ApiSnapshot a 
        WHERE a.name = :name AND a.namespace = :nm AND a.version <> :version
        ORDER BY a.modifiedDate DESC 
    """)
    fun findTopPreviousVersion(@Param("name") apiName: String, @Param("nm") namespace: String,
                               @Param("version") version: String,
                               pgConfig: Pageable): Page<ApiSnapshot>

}