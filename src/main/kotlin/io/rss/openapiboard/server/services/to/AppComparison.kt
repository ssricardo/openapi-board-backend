package io.rss.openapiboard.server.services.to

import io.rss.openapiboard.server.persistence.entities.AppSnapshot

/** Holds the 2 entities for a comparison. Both required */
data class AppComparison(val source: AppSnapshot,
                         val compared: AppSnapshot)
