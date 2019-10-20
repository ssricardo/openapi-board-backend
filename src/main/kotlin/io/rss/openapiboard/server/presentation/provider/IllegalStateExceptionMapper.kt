package io.rss.openapiboard.server.presentation.provider

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
                .entity(buildClientError(exception)).build()
    }

    private fun buildClientError(ex: IllegalStateException): String =
            "OPBOARD||00||${ex.message}"
}