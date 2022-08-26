package io.rss.openapiboard.server.services.exceptions

/**
 * General Application exception
 */
class BoardApplicationException(cause: String, rootCause: Exception? = null): IllegalStateException(cause, rootCause) {
}