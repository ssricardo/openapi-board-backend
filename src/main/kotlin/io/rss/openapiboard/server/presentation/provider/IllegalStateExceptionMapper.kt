package io.rss.openapiboard.server.presentation.provider

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * Maps IllegalStateException to client error (conflict)
 * */

@Provider
class IllegalStateExceptionMapper: ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(exception: IllegalArgumentException): Response {
        return Response.status(Response.Status.CONFLICT)
                .entity(buildClientError(exception)).build()
    }

    private fun buildClientError(ex: IllegalArgumentException): String =
            "OPBOARD||00||${ex.message}"
}