package io.rss.openapiboard.server.persistence.entities

import io.rss.openapiboard.server.persistence.AppOperationType
import javax.persistence.*

@Entity
@Table(name = "app_operation")
data class AppOperation(

        @Id
        @GeneratedValue
        var id: Int? = null
) {
    @JoinColumns(
            JoinColumn(name = "app_name", referencedColumnName = "name"),
            JoinColumn(name = "app_nspace", referencedColumnName = "namespace")
    )
    @ManyToOne(fetch = FetchType.LAZY)
    var appRecord: AppRecord? = null

    @Column(length = 350)
    var path: String? = null

    @Column(length = 10)
    @Enumerated
    var methodType: AppOperationType? = null
}