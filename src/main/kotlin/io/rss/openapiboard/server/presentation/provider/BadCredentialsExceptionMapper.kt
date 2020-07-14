package io.rss.openapiboard.server.presentation.provider

import org.springframework.security.authentication.BadCredentialsException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** Handles wrong User/pass or invalid token */
@Provider
class BadCredentialsExceptionMapper: ExceptionMapper<BadCredentialsException> {

    override fun toResponse(exception: BadCredentialsException): Response  =
            Response.status(Response.Status.UNAUTHORIZED)
                    .entity(exception.message)
                    .build()
}