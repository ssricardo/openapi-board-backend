package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RequestMemoryRepository: JpaRepository<RequestMemory, Long> {

    @Modifying
    @Query("DELETE FROM request_header h WHERE h.request_id = :requestId", nativeQuery = true)
    fun clearUpHeaders(@Param("requestId") id: Long)

    @Modifying
    @Query("DELETE FROM RequestMemory rm WHERE rm.id = :requestId AND rm.operation.id = :operationId")
    fun deleteOperationRequest(@Param("operationId") operationId: Int, @Param("requestId") requestId: Long)

    @Query("""
        SELECT r 
        FROM RequestMemory r
            JOIN FETCH r.operation oe 
            JOIN FETCH oe.appRecord ap 
        WHERE
            ap = :app 
    """)
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "request.headers")
    fun findByAppNamespace(app: AppRecord): List<RequestMemory>
}