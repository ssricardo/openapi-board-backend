package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AppOperation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppOperationRepository: JpaRepository<AppOperation, Int> {

    @Query("""SELECT o   
        FROM AppOperation o 
        WHERE o.appRecord.name = :appName AND  o.appRecord.namespace = :namespace""")
    fun findByAppNamespace(@Param("appName") appName: String, @Param("namespace") namespace: String): List<AppOperation>
}