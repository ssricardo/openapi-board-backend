package io.rss.openapiboard.server.persistence.entities.request

import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.RequestSampleAuthority
import org.hibernate.annotations.BatchSize
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotEmpty

/**
 * Represents a sample of a request to a given endpoint.
 */
@Entity
@Table(name = "requests")
@NamedEntityGraph(name = "request.parameters", attributeNodes = [
    NamedAttributeNode("parameters")
])
class RequestSample (

    @JoinColumn(nullable = false)
    @ManyToOne
    val operation: ApiOperation,

    @Id
    @GeneratedValue
    var id: Long? = null
): Serializable {

    @Column(length = 50, nullable = false)
    @NotEmpty
    var title: String = ""

    @Lob
    @Column(nullable = true)
    var body: String? = null

    @Column(name = "ns_attached", nullable = false)
    var namespaceAttached: Boolean = false

    @Column(nullable = false)
    @Enumerated
    var visibility: RequestVisibility = RequestVisibility.PUBLIC

    /* ex: application/json. Needed to match an entry on the OpenAPI definition */
    @Column(nullable =  false, length = 30)
    var contentType: String? = null

    @OneToMany(mappedBy = "request", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 15)    // WARN: provider specific
    val parameters = mutableListOf<ParameterSample>()

    @OneToMany(orphanRemoval = true, cascade = [CascadeType.ALL])
    val requiredAuthorities = mutableListOf<RequestSampleAuthority>()

    fun addParameterSample(pm: ParameterSample) {
        pm.request = this
        parameters.add(pm)
    }
}
