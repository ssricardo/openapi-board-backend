package io.rss.openapiboard.server.persistence.entities.request

import io.rss.openapiboard.server.persistence.entities.AppOperation
import org.hibernate.annotations.BatchSize
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Represents a sample of a request to a given endpoint.
 */
@Entity
@Table(name = "requests")
@NamedEntityGraph(name = "request.parameters", attributeNodes = [
    NamedAttributeNode("parameters")
])
data class RequestMemory (

        @Id
        @GeneratedValue
        var id: Long? = null
): Serializable {

    @JoinColumn(nullable = false)
    @ManyToOne
    var operation: AppOperation? = null

    @Column(length = 50, nullable = false)
    @NotEmpty
    var title: String = ""

    @Lob
    @Column(nullable = true)
    var body: String? = null

    @Column(name = "ns_attached", nullable = false)
    var nsAttached: Boolean = false

    @Column(nullable = false)
    @Enumerated
    @NotNull
    var visibility: RequestVisibility? = null

    /* ex: application/json. Needed to match an entry on the OpenAPI definition */
    @Column(nullable =  false, length = 30)
    var contentType: String? = null

    @OneToMany(mappedBy = "request", cascade = [CascadeType.ALL], orphanRemoval = true)
    @BatchSize(size = 30)    // WARN: provider specific :(
    val parameters = mutableListOf<ParameterMemory>()

    fun addParameterMemory(pm: ParameterMemory) {
        pm.request = this
        parameters.add(pm)
    }
}
