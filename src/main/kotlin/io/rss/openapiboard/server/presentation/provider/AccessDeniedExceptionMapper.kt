package io.rss.openapiboard.server.presentation.provider

import org.springframework.security.access.AccessDeniedException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** User is trying to access some resource not allowed for his roles */
@Provider
class AccessDeniedExceptionMapper: ExceptionMapper<AccessDeniedException> {

    override fun toResponse(exception: AccessDeniedException?) =
            Response.status(Response.Status.FORBIDDEN.statusCode, "You shall NOT pass!")
                    .build()
}