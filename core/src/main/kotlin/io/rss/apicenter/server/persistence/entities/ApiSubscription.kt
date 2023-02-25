package io.rss.apicenter.server.persistence.entities

import org.hibernate.validator.constraints.URL
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name="subscriptions", uniqueConstraints = [
    UniqueConstraint(name = "same_hook_api", columnNames = ["apiName", "targetWebhook"])
])
data class ApiSubscription (

    @Id
    @GeneratedValue
    var id: Long? = null
) {

    @Column(length = 128, nullable = false)
    @NotEmpty
    @URL
    var targetWebhook: String? = null

    @Column(length = 32, nullable = false)
    @NotEmpty
    var apiName: String? = null

    @Column(length = 32, nullable = true)
    var namepace: String? = null

    @Column
    var onlyOnChange: Boolean = false

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