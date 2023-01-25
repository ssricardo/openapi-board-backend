package io.rss.apicenter.server.web.provider

import io.rss.experimental.cleanUpStack
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.EntityNotFoundException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/** An Entity Not Found should never come over. If so, it's some part where this exception should not be possible */
@Provider
class EntityNotFoundExceptionMapper: ExceptionMapper<EntityNotFoundException> {

    override fun toResponse(exception: EntityNotFoundException): Response {
        LOG.warn("EntityNotFoundException raised on REST level.", exception.cleanUpStack())
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.statusCode,
                """The application was found in some inconsistent state.  
                    |Check whether the request is following the rules""".trimMargin())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build()
    }

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(EntityNotFoundExceptionMapper::class.java)
    }
}