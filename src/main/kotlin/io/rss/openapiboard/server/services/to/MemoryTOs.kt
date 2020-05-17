package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.AppOperationType

/** @see RequestMemoryResource */
data class RequestMemoryInputTO (val requestId: Long? = null,
                                 val namespace: String? = null, val appName: String? = null,
                                 val path: String? = null, val methodType: AppOperationType? = null) {

    val title: String? = null
    val body: String? = null
    val pathParameters: Map<String, String>? = null
    val queryParameters: Map<String, String>? = null
    val requestHeaders: Map<String, String>? = null
}