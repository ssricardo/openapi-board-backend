package io.rss.apicenter.server.services.to

import io.rss.apicenter.server.persistence.entities.ApiSubscription

/** Expose AlertSubscription to view */
data class SubscriptionRequestResponse (
    var hookAddress: String?,
    var apiName: String?,
    var id: Long? = null,
    var namespace: String? = null,
    var onlyOnChange: Boolean = false,
    var basePathList: List<String> = listOf()
) {

    fun toApiSubscription() =
        ApiSubscription(id).let { entity ->
            entity.apiName = apiName
            entity.targetWebhook = hookAddress
            entity.namepace = namespace
            entity.onlyOnChange = onlyOnChange
            entity.basePaths = basePathList.toMutableList()
            entity
        }
}

typealias SubscriptionRequest = SubscriptionRequestResponse
typealias SubscriptionResponse = SubscriptionRequestResponse

fun ApiSubscription.toRequestResponseTO() =
    SubscriptionResponse(targetWebhook, apiName, id, namepace, onlyOnChange, basePaths)

/** Carry result of retrieved data from Mail token */
data class SubscriptionMailId(val apiName: String, val email: String)

