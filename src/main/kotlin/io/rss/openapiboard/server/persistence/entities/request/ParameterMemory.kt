package io.rss.openapiboard.server.persistence.entities.request

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "parameter_memory")
data class ParameterMemory (

        @Id
        @GeneratedValue()
        var id: Long? = null) {

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    var kind: ParameterKind? = null

    @Column(length = 30)
    var name: String? = null

    @Column(length = 100)
    var value: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = true)
    var request: RequestMemory? = null
}

enum class ParameterKind {
    HEADER,
    QUERY,
    PATH,
    COOKIE,
    MATRIX
    ;
}