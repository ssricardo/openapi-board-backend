package io.rss.openapiboard.server.presentation.provider

import javax.validation.ConstraintViolationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.Providers

@Provider
class ConstraintViolationExceptionMapper: ExceptionMapper<ConstraintViolationException> {

    override fun toResponse(exception: ConstraintViolationException): Response {
        // TODO log

        return Response.status(Response.Status.CONFLICT)
                .entity(AppValidationError(exception.constraintViolations
                        .joinToString(System.lineSeparator()) { "${it.propertyPath.last()} : ${it.message}" }))
                .build()
    }

    private data class AppValidationError(val cause: String, val code: Int = 1) {
        val rApp = "OPBOARD"
    }
}