package io.rss.openapiboard.server.persistence.entities.request

/** Controls whether this request is visible to others */
enum class RequestVisibility {
    PUBLIC,

    USER,

    GROUP
}