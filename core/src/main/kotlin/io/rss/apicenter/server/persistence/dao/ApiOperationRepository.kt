package io.rss.apicenter.server.persistence.dao

import io.rss.apicenter.server.persistence.MethodType
import io.rss.apicenter.server.persistence.entities.ApiOperation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ApiOperationRepository: JpaRepository<ApiOperation, Int> {

    @Query("""SELECT o   
        FROM ApiOperation o 
        WHERE o.apiRecord.id = :apiId AND o.path = :path AND o.methodType = :opType
        """)
    fun findSingleMatch(@Param("apiId") apiId: UUID, @Param("path") path: String,
                        @Param("opType") method: MethodType): ApiOperation?
}