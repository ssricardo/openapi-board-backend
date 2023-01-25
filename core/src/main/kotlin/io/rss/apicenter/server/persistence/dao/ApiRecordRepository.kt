package io.rss.apicenter.server.persistence.dao

import io.rss.apicenter.server.persistence.ApiNamespace
import io.rss.apicenter.server.persistence.ApiVersionResponse
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ApiRecordRepository: JpaRepository<ApiRecord, UUID> {

    @Query("""
        SELECT a 
        FROM ApiRecord a 
        WHERE a.namespace = :ns AND a.name = :name
    """)
    fun findByNamespaceName(@Param("ns") namespace: String, @Param("name") name: String): ApiRecord?

    @Query("""
        SELECT new io.rss.apicenter.server.persistence.ApiNamespace(a.name, a.namespace) 
        FROM ApiRecord a 
        WHERE a.id = :apiId 
    """)
    fun findApiNamespace(@Param("apiId") apiId: UUID): ApiNamespace?

    @Query("""
        SELECT NEW io.rss.apicenter.server.persistence.ApiVersionResponse(a.id, a.name, a.version) 
        FROM ApiRecord a
        WHERE a.namespace = ?1
    """)
    fun findApiVersionByNamespace(namespace: String): List<ApiVersionResponse>

    @Query("""
        SELECT DISTINCT ar.id 
        FROM ApiRecord ar 
            LEFT JOIN ar.requiredAuthorities au  
        WHERE ar.id IN (:apiIdList) 
            AND (au.id IS NOT NULL AND (au.authority NOT IN (:userAuthorities)) )        
    """)
    fun findDeniedApisForAuthorities(@Param("apiIdList") apiIdList: List<UUID>,
                                     @Param("userAuthorities") authList: List<String>): List<UUID>

}