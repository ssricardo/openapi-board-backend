package io.rss.apicenter.server.persistence.entities.request

/** Controls whether this request is visible to others */
enum class RequestVisibility {
    PUBLIC,

    USER,

    GIVEN_AUTHORITY
}