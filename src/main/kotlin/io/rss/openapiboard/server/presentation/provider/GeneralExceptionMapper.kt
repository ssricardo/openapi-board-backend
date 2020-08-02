package io.rss.openapiboard.server.presentation.provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.ws.rs.ClientErrorException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.Providers

/**
 * General Handler for unexpected or wrapped exceptions
 */
@Provider
class GeneralExceptionMapper: ExceptionMapper<Exception> {

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(GeneralExceptionMapper::class.java)
    }

    @Context
    private lateinit var providers: Providers

    override fun toResponse(e: Exception): Response {
        lookUpSpecificHandler(e)?.let {
            return it
        }

        val clientErr = e as? ClientErrorException
        clientErr?.let {
            return it.response
        }

        LOG.error("Error captured on General Handler", e)
        return Response.serverError().entity(
                "The server had an unexpected error. Repeat the operation after some time. " +
                        "If the error persists, check the logs.").build()
    }

    /** Tries to find a more specific provider, in case of wrapped Exception */
    private fun lookUpSpecificHandler(e: Exception): Response? {
        var currentCause = e.cause
        var currentProvider: ExceptionMapper<Throwable?>? = currentCause?.javaClass?.let { providers.getExceptionMapper(it) }

        while (currentCause != null && currentProvider == null) {
            currentCause = currentCause.cause
            currentProvider =
                    currentCause?.javaClass?.let { providers.getExceptionMapper(it) }
        }
        return currentProvider?.toResponse(currentCause)
    }
}