package io.rss.openapiboard.server.services.to

/**
 * Wraps a query result.
 * Adds some metadata to it. Indicating for instance, whether the data is already complete.
 * Therefore helping FE to avoid not needed new queries */
class QueryResult<R> (
        val result: List<R>,
        val complete: Boolean
) {}