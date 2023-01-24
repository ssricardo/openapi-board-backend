package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Representation of RequestMemoryResource on View
 * @see RequestMemoryResource
 * */
data class MemoryRequestResponse (var requestId: Long? = null,
                                  @field:NotNull @field:NotEmpty val namespace: String? = null,
                                  @field:NotNull @field:NotEmpty val apiName: String? = null,
                                  @field:NotNull @field:NotEmpty val path: String? = null,
                                  @field:NotNull val methodType: MethodType? = null) {

    @NotNull
    var title: String? = null
    var body: String? = null
    var parameters = mutableListOf<ParameterMemoryTO>()
    var requestHeaders = mutableListOf<ParameterMemoryTO>()
    var requiredAuthorities: MutableList<String>? = mutableListOf()
}

data class ParameterMemoryTO(var id: Long? = null,
                             @field:NotNull var kind: ParameterType? = null,
                             @field:NotNull var name: String? = null,
                             @field:NotNull var value: String? = null
)