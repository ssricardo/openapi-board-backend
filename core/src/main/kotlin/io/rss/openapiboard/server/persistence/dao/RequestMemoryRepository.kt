package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RequestMemoryRepository: JpaRepository<RequestMemory, Long> {

    /** Retrieves memories related to given api+namespace PLUS the same api name on 'master' namespace */
    @Query("""
        SELECT r 
        FROM RequestMemory r
            JOIN FETCH r.operation oe 
            JOIN FETCH oe.apiRecord ap 
        WHERE
            (ap.name = :apiName AND ap.namespace = :ns) OR (ap.name = :apiName AND ap.namespace = 'master')
    """)
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "request.parameters")
    fun findByApiNamespace(@Param("apiName") api: String, @Param("ns") namespace: String): List<RequestMemory>

    @Query("""
        SELECT rm
        FROM RequestMemory rm 
            JOIN FETCH rm.operation op 
            JOIN FETCH op.apiRecord ar 
        WHERE (LOWER(rm.title) LIKE CONCAT(LOWER(:query), '%') ) OR (op.path LIKE CONCAT(LOWER(:query), '%') )
    """)
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "request.parameters")
    fun findRequestsByFilter(query: String, page: Pageable): List<RequestMemory>
}