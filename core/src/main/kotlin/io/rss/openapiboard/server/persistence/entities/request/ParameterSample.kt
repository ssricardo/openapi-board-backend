package io.rss.openapiboard.server.persistence.entities.request

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "parameter_sample")
data class ParameterSample (

        @Id
        @GeneratedValue
        var id: Long? = null): Serializable {

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    var parameterType: ParameterType? = null

    @Column(length = 30)
    var name: String? = null

    @Column(length = 256, name = "pvalue")
    var value: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = true)
    var request: RequestSample? = null
}

enum class ParameterType {
    HEADER,
    QUERY,
    PATH,
    COOKIE,
    MATRIX
    ;
}