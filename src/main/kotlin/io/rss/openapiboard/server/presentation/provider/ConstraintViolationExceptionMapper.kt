package io.rss.openapiboard.server.presentation.provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.Providers

@Provider
class ConstraintViolationExceptionMapper: ExceptionMapper<ConstraintViolationException> {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(ConstraintViolationExceptionMapper::class.java)
    }

    override fun toResponse(exception: ConstraintViolationException): Response {
        LOG.debug("Request not completed due constraint violations: ${exception.constraintViolations}")

        return Response.status(Response.Status.CONFLICT)
                .entity(AppValidationError(exception.constraintViolations
                        .joinToString(System.lineSeparator()) { "${it.propertyPath.last()} : ${it.message}" }))
                .build()
    }

    private data class AppValidationError(val cause: String, val code: Int = 1) {
        val rApp = "OPBOARD"
    }
}