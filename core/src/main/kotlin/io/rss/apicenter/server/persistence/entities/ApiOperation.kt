package io.rss.apicenter.server.persistence.entities

import io.rss.apicenter.server.persistence.MethodType
import org.apache.commons.codec.digest.DigestUtils
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "api_operation",
        uniqueConstraints = [UniqueConstraint(name = "operation_on_api", columnNames = ["unique_hash"])])
class ApiOperation(

        @JoinColumn(name = "api_id", nullable = false)
        @ManyToOne(fetch = FetchType.LAZY)
        val apiRecord: ApiRecord,

        @Id
        @GeneratedValue
        var id: Int? = null
) {

    @Column(length = 350)
    var path: String? = null

    @Column(length = 10)
    @Enumerated
    var methodType: MethodType? = null

    @Column(name = "unique_hash")
    protected var uniqueHash: String? = null

    /** Used for enforcing unique constraint. If the columns were used directly, in Mysql: error due to too long index */
    @PrePersist
    @PreUpdate
    protected fun updateHash() {
        val strLine = StringJoiner(":")
                .add(apiRecord.id.toString())
                .add(path)
                .add(methodType?.name)
        this.uniqueHash = DigestUtils.sha1Hex(strLine.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiOperation

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id ?: 0
    }

}