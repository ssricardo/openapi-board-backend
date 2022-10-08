package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AlertSubscriptionRepository: JpaRepository<AlertSubscription, Long> {

    @Query("""
        SELECT a  
        FROM AlertSubscription a  
        WHERE a.email = :mail AND a.apiName = :apiName
    """)
    fun findByMailApi(@Param("mail") mail: String,
                      @Param("apiName") apiName: String): AlertSubscription?

    @Query("""
        SELECT a 
        FROM AlertSubscription a 
        WHERE a.apiName = :name 
    """)
    fun findByApi(@Param("name") apiName: String): List<AlertSubscription>
}