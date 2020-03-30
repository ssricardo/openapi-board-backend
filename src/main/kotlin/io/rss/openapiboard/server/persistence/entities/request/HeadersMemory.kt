package io.rss.openapiboard.server.persistence.entities.request

import javax.persistence.*

@Entity
@Table(name = "request_header")
class HeadersMemory (

        @Id
        @GeneratedValue
        var id: Long? = null
) {

    @Column(length = 30, nullable = false)
    var name: String? = null

    @Column(length = 100)
    var value: String? = null

    @JoinColumn(name = "request_id")
    @ManyToOne
    var request: RequestMemory? = null
}