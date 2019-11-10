package io.rss.openapiboard.server.persistence

/** Holds Dtos used on "reduced" queries - Queries that doesn't take entire entities */

/** contains only app name and version. Used for querying a single namespace */
data class AppVersionDto(val name: String?, val version: String?)