package io.rss.experimental

import java.lang.Exception

private val collapsablePackages = listOf(
        "org.springframework.security",
        "org.springframework",
        "org.apache.catalina",
        "org.glassfish.jersey"
)

/**
 * The idea is to remove the lots of items in the stacktrace, which are of interest.
 * Instead of completely removing all entries from given packages, we keep the borders (the first and last in the given package),
 * since that can be useful sometimes.
 */

fun Exception.cleanUpStack(): Exception {
    var lastElement: StackTraceElement? = null
    var lastSkipped: StackTraceElement? = null
    var skippedCount = 0
    var resultStack: MutableList<StackTraceElement?> = MutableList(stackTrace.size) { null }

    stackTrace.forEach { element ->
        val collapsablePackage = matchCollapsablePackage(element.className)
        if (collapsablePackage == null || !isSamePackageAsLastElement(lastElement, collapsablePackage)) {
            lastSkipped?.let {
                if (skippedCount > 1) {
                    resultStack.add(StackTraceElement(it.className, "[...]", "skipped", skippedCount))
                }
                resultStack.add(it)
                lastSkipped = null
                skippedCount = 0
            }
            resultStack.add(element)
        } else {
            lastSkipped = element
            skippedCount++
        }

        lastElement = element
    }

    stackTrace = resultStack.filterNotNull().toTypedArray()
    return this
}

private fun isSamePackageAsLastElement(lastElement: StackTraceElement?, collapsablePackage: String) =
        lastElement?.className?.startsWith(collapsablePackage) == true

private fun matchCollapsablePackage(className: String): String? {
    return collapsablePackages.firstOrNull { className.startsWith(it) }
}
