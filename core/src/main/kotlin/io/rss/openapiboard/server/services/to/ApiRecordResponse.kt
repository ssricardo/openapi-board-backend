package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.entities.ApiRecord

class ApiRecordResponse (source: ApiRecord) {
    val name = source.name
    val namespace = source.namespace
    val version = source.version
    val basePath= source.basePath
    val lastModified = source.lastModified
}