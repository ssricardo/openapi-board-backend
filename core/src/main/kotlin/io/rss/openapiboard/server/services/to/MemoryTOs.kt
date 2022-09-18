package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import javax.validation.constraints.NotNull

/**
 * Representation of RequestMemoryResource on View
 * @see RequestMemoryResource */
data class RequestMemoryRequestResponse (var requestId: Long? = null,
                                         @NotNull val namespace: String? = null, @NotNull val apiName: String? = null,
                                         @NotNull val path: String? = null, @NotNull val methodType: MethodType? = null) {

    @NotNull
    var title: String? = null
    var body: String? = null
    var parameters = mutableListOf<ParameterMemoryTO>()
    var requestHeaders = mutableListOf<ParameterMemoryTO>()
}

data class ParameterMemoryTO(val id: Long? = null,
                             val kind: ParameterType? = null,
                             val name: String? = null,
                             val value: String? = null
)