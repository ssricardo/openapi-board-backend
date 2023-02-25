package io.rss.apicenter.server.persistence.dao

import io.rss.apicenter.server.persistence.entities.ApiSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApiSubscriptionRepository: JpaRepository<ApiSubscription, Long> {

    @Query(
        """
        SELECT a  
        FROM ApiSubscription a  
        WHERE a.targetWebhook = :hook AND a.apiName = :apiName
    """
    )
    fun findByHookApi(@Param("hook") hook: String,
                      @Param("apiName") apiName: String): ApiSubscription?

    @Query(
        """
        SELECT a 
        FROM ApiSubscription a 
        WHERE a.apiName = :name 
    """
    )
    fun findByApi(@Param("name") apiName: String): List<ApiSubscription>
}