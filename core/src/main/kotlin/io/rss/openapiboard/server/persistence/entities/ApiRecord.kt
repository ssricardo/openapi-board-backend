package io.rss.openapiboard.server.persistence.entities


import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

/** Main Entity in the app. Represents the current state of an API, for a given namespace */
@Table(name = "api_records")
@Entity
class ApiRecord(

        @field:Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid api name")
        @Column(nullable = false)
        var name: String,

        @field:Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid namespace name")
        @Column(name = "ns_id"/*, columnDefinition = "VARCHAR(50) FOREIGN KEY REFERENCES namespaces(name)"*/)
        var namespace: String,

        @Column(name = "api_version", length = 30)
        @field:NotEmpty
        var version: String,

        @Id
        @GeneratedValue(generator = "hibernate-uuid")
        @GenericGenerator(name = "hibernate-uuid", strategy = "uuid2")
        @Type(type="uuid-char")
        var id: UUID? = null
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

        override fun toString(): String {
                return "ApiRecord(name='$name', id=$id)"
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ApiRecord

                if (id == null) return super.equals(other)

                if (id != other.id) return false

                return true
        }

        override fun hashCode(): Int {
                return id?.hashCode() ?: 0
        }

}