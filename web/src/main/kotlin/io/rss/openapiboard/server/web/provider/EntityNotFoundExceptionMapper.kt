package io.rss.openapiboard.server.web.provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.EntityNotFoundException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** An Entity Not Found should never come over. If so, it's some part where this exception should not be possible */
@Provider
class EntityNotFoundExceptionMapper: ExceptionMapper<EntityNotFoundException> {

    override fun toResponse(exception: EntityNotFoundException): Response {
        LOG.warn("EntityNotFoundException raised on REST level.", exception)
        return Response.status(Response.Status.BAD_REQUEST.statusCode,
                """The application was found in some inconsistent state.  
                    |Check whether the request is following the rules""".trimMargin()).build()
    }

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(EntityNotFoundExceptionMapper::class.java)
    }
}