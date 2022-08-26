package io.rss.openapiboard.server.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @see AuthenticationEntryPoint
 * */
@Component
class AuthEntryPointJwt: AuthenticationEntryPoint {

    private val logger: Logger = LoggerFactory.getLogger(AuthEntryPointJwt::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse,
                 authException: AuthenticationException) {
        logger.error("Unauthorized error: {}", authException.message)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized")
    }
}