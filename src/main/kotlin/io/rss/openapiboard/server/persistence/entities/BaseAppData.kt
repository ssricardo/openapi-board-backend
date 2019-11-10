package io.rss.openapiboard.server.persistence.entities

import java.time.LocalDateTime
import javax.persistence.*

/** Common mapping for AppRegistry and AppSnapshot */
@MappedSuperclass
abstract class BaseAppData {

    @Column(name="app_version", length = 26)
    open var version: String? = null

    @Column(name="api_source")
    @Lob
    var source: String? = null

    @Column(length = 260)
    var address: String? = null

    @Column
    var modifiedDate: LocalDateTime? = null

    @PrePersist
    @PreUpdate
    protected fun updateDate() {
        modifiedDate = LocalDateTime.now()
    }
}