package io.rss.apicenter.server.persistence.entities

import java.time.LocalDateTime
import javax.persistence.*

/** Common mapping for ApiRegistry and ApiSnapshot */
@MappedSuperclass
abstract class BaseApiData {

    @Column(name="api_source")
    @Lob
    var source: String? = null

    @Column(length = 260)
    var apiUrl: String? = null

    @Column
    lateinit var modifiedDate: LocalDateTime

    @PrePersist
    @PreUpdate
    fun updateDate() {
        modifiedDate = LocalDateTime.now()
    }
}