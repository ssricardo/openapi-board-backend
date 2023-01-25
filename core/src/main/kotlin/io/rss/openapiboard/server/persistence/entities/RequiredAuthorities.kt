package io.rss.openapiboard.server.persistence.entities

import io.rss.openapiboard.server.persistence.entities.request.RequestSample
import javax.persistence.*

@Entity
@Table(name = "required_auths")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, length = 3)
open class RequiredAuthorities (

        @Column
        open val authority: String,

        @Id
        @GeneratedValue
        open var id: Int? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequiredAuthorities

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}

@Entity
@DiscriminatorValue("api")
class ApiAuthority (

    @ManyToOne
    @JoinColumn(name = "api_id")
    val apiRecord: ApiRecord,

    authority: String
): RequiredAuthorities (authority)

@Entity
@DiscriminatorValue("ns")
class NamespaceAuthority (

    @ManyToOne
    @JoinColumn(name = "ns_id")
    var namespace: Namespace,

    authority: String
): RequiredAuthorities (authority)

@Entity
@DiscriminatorValue("req")
class RequestSampleAuthority(

        @ManyToOne
        @JoinColumn(name = "req_id")
        val requestSample: RequestSample,

        authority: String
) : RequiredAuthorities(authority)