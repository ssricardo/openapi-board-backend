package io.rss.openapiboard.server.persistence

/** Holds Dtos used on "reduced" queries - Queries that don't take entire entities */

/** contains only api name and version. Used for querying a single namespace */
data class ApiVersionDto(val name: String?, val version: String?)