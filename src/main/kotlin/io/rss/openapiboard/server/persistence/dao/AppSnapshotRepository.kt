package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.persistence.entities.AppSnapshot
import io.rss.openapiboard.server.persistence.entities.AppSnapshotId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppSnapshotRepository: JpaRepository<AppSnapshot, AppSnapshotId> {

    @Query("""
       SELECT DISTINCT a.version
       FROM AppSnapshot a
       WHERE a.name = :#{#app.name} AND a.namespace = :#{#app.namespace}
    """)
    fun findAppVersionList(@Param("app") appId: AppRecordId): List<String>

}