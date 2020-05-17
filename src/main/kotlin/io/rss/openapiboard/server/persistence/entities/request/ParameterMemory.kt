package io.rss.openapiboard.server.persistence.entities.request

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "parameter_memory")
@IdClass(ParameterMemoryId::class)
data class ParameterMemory (

        @Id
        @Column(length = 10)
        @Enumerated(EnumType.STRING)
        var kind: ParameterKind? = null,

        @Id
        @Column(length = 30)
        var name: String? = null) {

    @Column(length = 100)
    var value: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    var request: RequestMemory? = null
}

@Embeddable
data class ParameterMemoryId (var kind: ParameterKind? = null,
                              var name: String? = null): Serializable

enum class ParameterKind {
    QUERY,
    PATH,
    COOKIE,
    MATRIX;
}