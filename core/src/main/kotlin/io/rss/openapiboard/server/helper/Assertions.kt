package io.rss.openapiboard.server.helper

import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import java.util.*

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

inline fun <T : Any?> assertRequired(expression: T?, lazyMessage: () -> String): T {
    if (expression == null) {
        throw BoardApplicationException(lazyMessage(), null)
    }
    return expression
}

fun assertStringRequired(expression: String?, lazyMessage: () -> String) {
    if (expression.isNullOrEmpty()) {
        throw BoardApplicationException(lazyMessage(), null)
    }
}

fun assertGetStringsRequired(lazyMessage: () -> String, vararg expressions: String?): List<String> {
    repeat(expressions.count()) {
        if (expressions[it].isNullOrEmpty()) {
            throw BoardApplicationException(lazyMessage(), null)
        }
    }
    return expressions.asList().filterNotNull()
}