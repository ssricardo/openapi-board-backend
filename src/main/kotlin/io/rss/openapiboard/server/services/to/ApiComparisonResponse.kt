package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.entities.ApiSnapshot

/** Holds the 2 entities for a comparison. Both required */
data class ApiComparisonResponse(val source: ApiSnapshot,
                                 val compared: ApiSnapshot)
