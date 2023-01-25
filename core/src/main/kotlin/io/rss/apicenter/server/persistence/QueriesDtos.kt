package io.rss.apicenter.server.persistence

import java.util.UUID

/** Holds Dtos used on "reduced" queries - Queries that don't take entire entities */

/** For a given API, returns only its name and namespace */
data class ApiNamespace(val apiName: String, val namespace: String)

/** contains only api name and version. Used for querying a single namespace */
data class ApiVersionResponse(val apiId: UUID, val name: String, val version: String?)