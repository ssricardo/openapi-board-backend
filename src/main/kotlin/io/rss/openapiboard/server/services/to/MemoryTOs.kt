package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.AppOperationType
import io.rss.openapiboard.server.persistence.entities.request.ParameterKind

/**
 * Representation of RequestMemoryResource on View
 * @see RequestMemoryResource */
data class RequestMemoryViewTO (val requestId: Long? = null,
                                val namespace: String? = null, val appName: String? = null,
                                val path: String? = null, val methodType: AppOperationType? = null) {

    var title: String? = null
    var body: String? = null
    var parameters = mutableListOf<ParameterMemoryTO>()
    var requestHeaders = mutableListOf<ParameterMemoryTO>()
}

data class ParameterMemoryTO(val id: Long? = null,
                             val kind: ParameterKind? = null,
                             val name: String? = null,
                             val value: String? = null
)