package io.rss.openapiboard.server.security

import io.rss.openapiboard.server.helper.AuthHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.lang.RuntimeException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** As the name says...
 * @see SecurityConfig
 * */
class AuthTokenFilter : OncePerRequestFilter() {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
        private const val BEARER_PREFIX = "Bearer "
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            verifyAndGetJwt(request)?.let {
                val authentication = UsernamePasswordAuthenticationToken(it.username, null, it.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: RuntimeException) {
            LOGGER.error("Cannot set user authentication: {}", e)
        }
        filterChain.doFilter(request, response)
    }


    private fun verifyAndGetJwt(request: HttpServletRequest): User? {
        val headerAuth = request.getHeader("Authorization")
        return if (headerAuth?.startsWith(BEARER_PREFIX) == true) {
            val token = headerAuth!!.removePrefix(BEARER_PREFIX)
            AuthHelper.validateConvertToUser(token)
        } else
            null
    }


}