package io.rss.openapiboard.server.services.to

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import java.time.LocalDateTime

/** Expose AlertSubscription to view */
@JsonDeserialize(using = SubscriptionDeserializer::class)   // See below
data class SubscriptionRequestResponse(private val wrapped: AlertSubscription = AlertSubscription()) {

    var id: Long?
        get() = wrapped.id
        set(value) {
            wrapped.id = value
        }

    var email: String?
        get() = wrapped.email
        set(value) {
            wrapped.email = requireNotNull(value)
        }

    var apiName
        get() = wrapped.apiName
        set(value) {
            wrapped.apiName = value
        }

    var modifiedTime: LocalDateTime?
        get() = wrapped.modifiedTime

        @JsonIgnore
        set(_) {
            // ignore
        }

    var basePathList
        get() = wrapped.basePaths
        set(value) {
            wrapped.basePaths = value
        }

    fun unwrap(): AlertSubscription {
        return wrapped
    }

}

/* Simple deserializer. Only to overcome issue that with default one, 'basePathList' was always null.
 * TODO: The real problem should be fixed, and after that this should be removed
 * */
class SubscriptionDeserializer: JsonDeserializer<SubscriptionRequestResponse>() {

    private val mapper = ObjectMapper()

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SubscriptionRequestResponse {
        val node: JsonNode = p.codec.readTree(p)

        return SubscriptionRequestResponse().apply {
            id = if (node.has("id") && node.get("id").isLong) node.get("id").asLong() else null
            email = if (node.has("email")) node.get("email").asText() else null
            if (node.has("apiName")) {
                apiName = node.get("apiName").asText()
            }
            basePathList =
                    mapper.reader().forType(MutableList::class.java).readValue(
                            node.get("basePathList").toString())

        }
    }

}

/** Carry result of retrieved data from Mail token */
data class SubscriptionMailId(val apiName: String, val email: String)