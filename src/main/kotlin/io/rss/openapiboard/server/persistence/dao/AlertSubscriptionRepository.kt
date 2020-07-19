package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AlertSubscriptionRepository: JpaRepository<AlertSubscription, Long> {

    @Query("""
        DELETE FROM AlertSubscription a 
        WHERE a.email = :mail AND a.appName = :appId
    """)
    @Modifying
    fun deleteByMailApp(@Param("mail") mail: String,
                        @Param("appId") appId: String)

    @Query("""
        SELECT a 
        FROM AlertSubscription a 
        WHERE a.appName = :name 
    """)
    fun findByApp(@Param("name") appName: String): List<AlertSubscription>
}