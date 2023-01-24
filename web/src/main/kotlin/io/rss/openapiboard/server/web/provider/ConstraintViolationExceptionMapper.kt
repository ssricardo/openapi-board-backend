package io.rss.openapiboard.server.web.provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ConstraintViolationExceptionMapper: ExceptionMapper<ConstraintViolationException> {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(ConstraintViolationExceptionMapper::class.java)
    }

    override fun toResponse(exception: ConstraintViolationException): Response {
        LOG.debug("Request not completed due constraint violations: ${exception.constraintViolations}")

        val causeDescription = exception.constraintViolations
                .joinToString(System.lineSeparator()) { "${it.propertyPath.last()} : ${it.message}" }

        return Response.status(Response.Status.CONFLICT)
                .entity(AppValidationError(causeDescription,ErrorCodeType.CONSTRAINT_VIOLATION))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build()
    }
}