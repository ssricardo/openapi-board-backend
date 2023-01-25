package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.request.RequestSample
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RequestSampleRepository: JpaRepository<RequestSample, Long> {

    /** Retrieves samples related to given api+namespace PLUS the same api name on 'master' namespace */
    @Query("""
        SELECT r 
        FROM RequestSample r
            JOIN FETCH r.operation oe 
            JOIN FETCH oe.apiRecord ap 
        WHERE
            (ap.name = :apiName AND ap.namespace = :ns) OR (ap.name = :apiName AND ap.namespace = 'master')
    """)
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "request.parameters")
    fun findByApiNamespace(@Param("apiName") api: String, @Param("ns") namespace: String): List<RequestSample>

    @Query("""
        SELECT rs 
        FROM RequestSample rs 
            JOIN FETCH rs.operation op 
            JOIN FETCH op.apiRecord ar 
        WHERE (LOWER(rs.title) LIKE CONCAT(LOWER(:query), '%') ) OR (op.path LIKE CONCAT(LOWER(:query), '%') )
    """)
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "request.parameters")
    fun findRequestsByFilter(@Param("query") query: String, page: Pageable): List<RequestSample>


    @Query("""
        SELECT DISTINCT rs.id  
        FROM RequestSample rs 
            LEFT JOIN rs.requiredAuthorities rsa 
            JOIN rs.operation.apiRecord ar 
            LEFT JOIN ApiAuthority apia ON apia.apiRecord.id = ar.id 
        WHERE rs.id IN (:idList)
            AND (
                (rsa.id IS NOT NULL AND rsa.authority NOT IN (:userAuthorities)) 
                OR 
                (apia.id IS NOT NULL AND apia.authority NOT IN (:userAuthorities))
            )  
    """)
    fun findDeniedSamplesForAuthorities(@Param("idList") requestIdList: List<Long>,
                                        @Param("userAuthorities") authoritiesString: List<String>): List<Long>
}