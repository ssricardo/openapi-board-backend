package io.rss.openapiboard.server.web.provider

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * Maps IllegalStateException to client error (conflict)
 * */

@Provider
class IllegalStateExceptionMapper: ExceptionMapper<IllegalStateException> {

    override fun toResponse(exception: IllegalStateException): Response {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(buildClientError(exception)).build()
    }

    data class AppValidationError(val cause: String, val code: Int = 0) {
        val rApp = "OPBOARD"
    }

    private fun buildClientError(ex: IllegalStateException) =
            AppValidationError(ex.message
                    ?: throw IllegalArgumentException("Validation error MUST have a cause description"))
}