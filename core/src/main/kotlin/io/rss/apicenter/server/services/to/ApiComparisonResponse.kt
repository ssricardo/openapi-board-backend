package io.rss.apicenter.server.services.to

import io.rss.apicenter.server.persistence.entities.ApiSnapshot

/** Holds the 2 entities for a comparison. Both required */
data class ApiComparisonResponse(val source: ApiSnapshot,
                                 val compared: ApiSnapshot)
