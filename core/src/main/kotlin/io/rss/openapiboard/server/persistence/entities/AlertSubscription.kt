package io.rss.openapiboard.server.persistence.entities

import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
@Table(name="alert_subs", uniqueConstraints = [
    UniqueConstraint(name = "same_mail_api", columnNames = ["apiName", "email"])
])
data class AlertSubscription (

    @Id
    @GeneratedValue
    var id: Long? = null
) {

    @Column(length = 64, nullable = false)
    @NotEmpty
    var email: String? = null

    @Column(length = 32, nullable = false)
    @NotEmpty
    var apiName: String? = null

    @Column
    lateinit var modifiedTime: LocalDateTime

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "base_path")
    var basePaths = mutableListOf<String>()

    @PrePersist
    @PreUpdate
    internal fun updateTime() {
        modifiedTime = LocalDateTime.now()
    }
}