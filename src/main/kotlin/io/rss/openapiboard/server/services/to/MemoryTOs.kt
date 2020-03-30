package io.rss.openapiboard.server.services.to

/** @see RequestMemoryResource */
data class RequestMemoryInputTO (val operationId: Int, val requestId: Long,
                                 val title: String?, val body: String,
                                 val requestHeaders: Map<String, String>? = null)