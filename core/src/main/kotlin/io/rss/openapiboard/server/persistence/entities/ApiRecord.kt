package io.rss.openapiboard.server.persistence.entities

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Pattern

/** Main Entity in the app. Represents the current state of an API, for a given namespace */
@Table(name = "api_record")
@Entity
@IdClass(ApiRecordId::class)
data class ApiRecord(

        @Id
        @Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid api name")
        var name: String? = null,

        @Id
        @Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid namespace name")
        var namespace: String? = null) : BaseApiData() {

        @Column(name = "api_version", length = 30)
        var version: String? = null

        @Column(name = "base_path", length = 200)
        var basePath: String? = null

        @Column(name = "date_modified")
        lateinit var lastModified: LocalDateTime

        @ElementCollection(fetch = FetchType.LAZY)
        @CollectionTable(name = "api_authorities")
        var allowedAuthorities = mutableListOf<String>()

        @PreUpdate
        @PrePersist
        fun updateModifiedDate() {
            lastModified = LocalDateTime.now()
        }
}

@Embeddable
data class ApiRecordId (

    var name: String? = null,

    var namespace: String? = null

): Serializable