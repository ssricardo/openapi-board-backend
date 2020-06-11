package io.rss.openapiboard.server.helper

import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import java.lang.IllegalStateException

/**
 * Helper functions with Assertions throwing own business exception
 */

fun assertValid(expression: Boolean, lazyMessage: () -> String) {
    if (! expression) {
        throw BoardApplicationException(lazyMessage(), null)
    }
}

/** Validates and throws when needed.
 * @throws IllegalStateException
 * */
inline fun assertState(expression: Boolean, lazyMessage: () -> String) {
    if (! expression) {
        throw IllegalStateException(lazyMessage())
    }
}

fun assertRequired(expression: Any?, lazyMessage: () -> String) {
    if (expression == null) {
        throw BoardApplicationException(lazyMessage(), null)
    }
}

fun assertStringRequired(expression: String?, lazyMessage: () -> String) {
    if (expression.isNullOrEmpty()) {
        throw BoardApplicationException(lazyMessage(), null)
    }
}