package io.rss.openapiboard.server.web.provider

import org.springframework.security.authentication.BadCredentialsException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** Handles wrong User/pass or invalid token */
@Provider
class BadCredentialsExceptionMapper: ExceptionMapper<BadCredentialsException> {

    override fun toResponse(exception: BadCredentialsException): Response  =
            Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(exception.message)
                    .build()
}