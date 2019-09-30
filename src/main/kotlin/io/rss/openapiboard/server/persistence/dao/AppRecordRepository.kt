package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AppRecordRepository: JpaRepository<AppRecord, AppRecordId> {

    @Query("""
        SELECT DISTINCT a.namespace
        FROM AppRecord a
    """)
    fun findAllNamespace(): List<String>

    @Query("""
        SELECT a.name
        FROM AppRecord a
        WHERE a.namespace = ?1
    """)
    fun findNamesByNamespace(namespace: String): List<String>

}