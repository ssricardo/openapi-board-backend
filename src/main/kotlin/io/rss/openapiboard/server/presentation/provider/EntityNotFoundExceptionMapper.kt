package io.rss.openapiboard.server.presentation.provider

import javax.persistence.EntityNotFoundException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** An Entity Not Found should never come over. If so, it's some part where this exception should not be possible */
@Provider
class EntityNotFoundExceptionMapper: ExceptionMapper<EntityNotFoundException> {

    override fun toResponse(exception: EntityNotFoundException): Response {
        // TODO log
        return Response.status(Response.Status.BAD_REQUEST.statusCode,
                """The application was found in some inconsistent state.  
                    |Check whether the request is following the rules""".trimMargin()).build()
    }
}