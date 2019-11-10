package io.rss.openapiboard.server.helper

import io.rss.openapiboard.server.services.exceptions.BoardApplicationException

/**
 * Helper functions with Assertions throws own business exception
 */

fun assertValid(expression: Boolean, lazyMessage: () -> String) {
    if (! expression) {
        throw BoardApplicationException(lazyMessage(), null)
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