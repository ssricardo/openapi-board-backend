package io.rss.openapiboard.server.persistence.entities

import io.rss.openapiboard.server.persistence.MethodType
import javax.persistence.*

@Entity
@Table(name = "api_operation",
        uniqueConstraints = [UniqueConstraint(name = "operation_on_api", columnNames = ["api_name", "api_nspace", "path", "methodType"])])
data class ApiOperation(

        @Id
        @GeneratedValue
        var id: Int? = null
) {
    @JoinColumns(
            JoinColumn(name = "api_name", referencedColumnName = "name"),
            JoinColumn(name = "api_nspace", referencedColumnName = "namespace")
    )
    @ManyToOne(fetch = FetchType.LAZY)
    var apiRecord: ApiRecord? = null

    @Column(length = 350)
    var path: String? = null

    @Column(length = 10)
    @Enumerated
    var methodType: MethodType? = null
}