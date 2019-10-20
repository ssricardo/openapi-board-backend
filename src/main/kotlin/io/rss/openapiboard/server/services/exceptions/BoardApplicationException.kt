package io.rss.openapiboard.server.services.exceptions

import java.lang.Exception
import java.lang.RuntimeException

/**
 * General Application exception
 */
class BoardApplicationException(cause: String, rootCause: Exception? = null): RuntimeException(cause, rootCause) {
}