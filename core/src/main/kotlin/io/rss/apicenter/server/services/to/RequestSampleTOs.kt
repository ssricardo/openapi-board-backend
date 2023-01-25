package io.rss.apicenter.server.services.to

import io.rss.apicenter.server.persistence.MethodType
import io.rss.apicenter.server.persistence.entities.request.ParameterType
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Representation of RequestSampleResource on View
 * @see RequestSampleResource
 * */
data class RequestSampleTO (var requestId: Long? = null,
                            @field:NotNull @field:NotEmpty val namespace: String? = null,
                            @field:NotNull @field:NotEmpty val apiName: String? = null,
                            @field:NotNull @field:NotEmpty val path: String? = null,
                            @field:NotNull val methodType: MethodType? = null) {

    @NotNull
    var title: String? = null
    var body: String? = null
    var parameters = mutableListOf<ParameterSampleTO>()
    var requestHeaders = mutableListOf<ParameterSampleTO>()
    var requiredAuthorities: MutableList<String>? = mutableListOf()
}

data class ParameterSampleTO(var id: Long? = null,
                             @field:NotNull var kind: ParameterType? = null,
                             @field:NotNull var name: String? = null,
                             @field:NotNull var value: String? = null
)

/* Alias for semantic only */
typealias RequestSampleInput = RequestSampleTO
typealias RequestSampleResponse = RequestSampleTO