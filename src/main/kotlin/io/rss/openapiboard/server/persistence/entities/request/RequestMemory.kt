package io.rss.openapiboard.server.persistence.entities.request

import io.rss.openapiboard.server.persistence.entities.AppOperation
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * Represents a sample of a request to a given endpoint.
 */
@Entity
@Table(name = "requests")
@NamedEntityGraph(name = "request.headers", attributeNodes = [
    NamedAttributeNode("headers")
])
data class RequestMemory (

        @Id
        @GeneratedValue
        var id: Long? = null
) {

    @JoinColumn(nullable = false)
    @ManyToOne
    var operation: AppOperation? = null

    @Column(length = 50, nullable = false)
    @NotNull
    var title: String? = null

    @Lob
    @Column(nullable = false)
    @NotNull
    var body: String? = null

    @Column
    var owner: Int? = null

    @Column(nullable = false)
    @Enumerated
    @NotNull
    var visibility: RequestVisibility? = null

    @Column(nullable =  false, length = 30)
    var contentType: String? = null

    @OneToMany(mappedBy = "request", cascade = [CascadeType.ALL], orphanRemoval = true)
    val headers: MutableList<HeadersMemory>  = mutableListOf()
}
