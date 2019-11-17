package io.rss.openapiboard.server.presentation.provider

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import kotlin.IllegalArgumentException

/**
 * Handles IllegalArgumentExceptionMapper
 *
 * IllegalArgument means that the request was not formatted as it should be
 */
@Provider
class IllegalArgumentExceptionMapper: ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(exception: IllegalArgumentException): Response {
        return Response.status(Response.Status.BAD_REQUEST.statusCode,
                exception.message).build()
    }
}