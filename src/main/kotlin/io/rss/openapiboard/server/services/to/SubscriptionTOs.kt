package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import java.time.LocalDateTime

/** Alert */
data class SubscriptionTO(private val wrapped: AlertSubscription = AlertSubscription()) {

    var email: String?
        get() = wrapped.email
        set(value) {
            wrapped.email = value
        }

    var appName
        get() = wrapped.appName
        set(value) {
            wrapped.appName = value
        }

    var modifiedTime: LocalDateTime
        get() = wrapped.modifiedTime
        set(value) {
            wrapped.modifiedTime = modifiedTime
        }

    var basePaths
        get() = wrapped.basePaths
        set(value) {
            wrapped.basePaths = value
        }

    fun unwrap(): AlertSubscription {
        return wrapped
    }

}

data class SubscriptionMailId(val appName: String, val email: String)