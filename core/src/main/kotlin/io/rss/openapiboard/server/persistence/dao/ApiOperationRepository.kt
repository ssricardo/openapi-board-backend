package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApiOperationRepository: JpaRepository<ApiOperation, Int> {

    @Query("""SELECT o   
        FROM ApiOperation o 
        WHERE o.apiRecord.name = :apiName AND  o.apiRecord.namespace = :namespace""")
    fun findByApiNamespace(@Param("apiName") apiName: String, @Param("namespace") namespace: String): List<ApiOperation>

    @Query("""SELECT o   
        FROM ApiOperation o 
        WHERE o.apiRecord.name = :apiName AND  o.apiRecord.namespace = :namespace
            AND o.path = :path AND o.methodType = :opType
        """)
    fun findSingleMatch(@Param("apiName") apiName: String, @Param("namespace") namespace: String,
                           @Param("path") path: String, @Param("opType") method: MethodType): ApiOperation?
}