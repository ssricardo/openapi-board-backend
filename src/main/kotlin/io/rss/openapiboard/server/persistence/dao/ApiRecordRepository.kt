package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.ApiVersionDto
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ApiRecordRepository: JpaRepository<ApiRecord, ApiRecordId> {

    @Query("""
        SELECT DISTINCT a.namespace
        FROM ApiRecord a
    """)
    fun findAllNamespace(): List<String>

    @Query("""
        SELECT NEW io.rss.openapiboard.server.persistence.ApiVersionDto(a.name, a.version) 
        FROM ApiRecord a
        WHERE a.namespace = ?1
    """)
    fun findApiListByNamespace(namespace: String): List<ApiVersionDto>

}