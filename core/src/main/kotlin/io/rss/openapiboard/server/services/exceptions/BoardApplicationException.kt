package io.rss.openapiboard.server.services.exceptions

/**
 * Business Application exception
 */
class BoardApplicationException(cause: String, rootCause: Exception? = null): IllegalStateException(cause, rootCause) {
}