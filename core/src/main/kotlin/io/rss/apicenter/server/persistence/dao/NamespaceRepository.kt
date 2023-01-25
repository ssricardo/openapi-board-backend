package io.rss.apicenter.server.persistence.dao

import io.rss.apicenter.server.persistence.entities.Namespace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NamespaceRepository: JpaRepository<Namespace, String> {

    @Query("""
        SELECT n 
        FROM Namespace n
            LEFT JOIN FETCH n.requiredAuthorities 
    """)
    fun findAllWithAuthorities(): List<Namespace>
}