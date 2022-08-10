package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.entities.request.ParameterType

/**
 * Representation of RequestMemoryResource on View
 * @see RequestMemoryResource */
data class RequestMemoryViewTO (val requestId: Long? = null,
                                val namespace: String? = null, val apiName: String? = null,
                                val path: String? = null, val methodType: MethodType? = null) {

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