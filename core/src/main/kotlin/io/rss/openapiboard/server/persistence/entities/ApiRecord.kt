package io.rss.openapiboard.server.persistence.entities


import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

/** Main Entity in the app. Represents the current state of an API, for a given namespace */
@Table(name = "api_records")
@Entity
@IdClass(ApiRecordId::class)
class ApiRecord(

        @Id
        @field:Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid api name")
        val name: String,

        @Id
        @Column(name = "ns_id"/*, columnDefinition = "VARCHAR(50) FOREIGN KEY REFERENCES namespaces(name)"*/)
        @field:Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid namespace name")
        val namespace: String,

        @Column(name = "api_version", length = 30)
        @field:NotEmpty
        var version: String
) : BaseApiData() {

        @Column(name = "base_path", length = 200)
        var basePath: String? = null

        @Column(name = "date_modified")
        lateinit var lastModified: LocalDateTime
                protected set

        @PreUpdate
        @PrePersist
        fun updateModifiedDate() {
            lastModified = LocalDateTime.now()
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ApiRecord

                if (name != other.name) return false
                if (namespace != other.namespace) return false

                return true
        }

        override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + namespace.hashCode()
                return result
        }
}

@Embeddable
data class ApiRecordId (

    var name: String? = null,

//    @Column(name = "ns_id", columnDefinition = "FOREIGN KEY REFERENCES namespaces(name)")
    var namespace: String? = null

): Serializable