package io.rss.openapiboard.server.presentation.provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.ws.rs.ClientErrorException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * General Handler for unexpected exceptions
 */
@Provider
class GeneralExceptionMapper: ExceptionMapper<Exception> {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(GeneralExceptionMapper::class.java)
    }

    override fun toResponse(e: Exception): Response {
        val clientErr = e as? ClientErrorException
        clientErr?.let {
            return it.response
        }
        LOG.error("Error captured on General Handler", e)
        return Response.serverError().entity(
                "The server had an unexpected error. Repeat the operation after some time. " +
                        "If the error persists, check the logs.").build()
    }
}