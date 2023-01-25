package io.rss.apicenter.server.web.provider

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * Handles IllegalArgumentExceptionMapper
 *
 * IllegalArgument means that the request was not formatted as it should be
 */
@Provider
class IllegalArgumentExceptionMapper: ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(exception: IllegalArgumentException): Response {

        return Response.status(Response.Status.BAD_REQUEST.statusCode, exception.message)
                .entity(AppValidationError(exception.message ?: exception.cause?.message ?: "unspecified",
                        ErrorCodeType.VALIDATION_FAIL))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build()
    }


}