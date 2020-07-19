package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.persistence.entities.AppSnapshot
import io.rss.openapiboard.server.persistence.entities.AppSnapshotId
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppSnapshotRepository: PagingAndSortingRepository<AppSnapshot, AppSnapshotId> {

    @Query("""
       SELECT DISTINCT a.version
       FROM AppSnapshot a
       WHERE a.name = :#{#app.name} AND a.namespace = :#{#app.namespace}
    """)
    fun findAppVersionList(@Param("app") appId: AppRecordId): List<String>

    @Query("""
        SELECT a 
        FROM AppSnapshot a 
        WHERE a.name = :name AND a.namespace = :nm AND a.version <> :version
            AND ROWNUM = 1 
        ORDER BY a.modifiedDate DESC 
    """)
    fun findTopPreviousVersion(@Param("name") appName: String, @Param("nm") namespace: String,
                               @Param("version") version: String): AppSnapshot?

}