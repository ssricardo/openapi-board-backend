package io.rss.apicenter.server.security

import io.rss.apicenter.server.helper.TokenHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** As the name says...
 * @see SecurityConfig
 * */
class AuthTokenFilter(private val authManager: AuthenticationManager) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            verifyAndGetJwt(request)?.let {
                val authentication = UsernamePasswordAuthenticationToken(it.username, null, it.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            } ?: tryBasicAuth(request)
        } catch (e: RuntimeException) {
            LOGGER.error("Cannot set user authentication: {}", e)
        }
        filterChain.doFilter(request, response)
    }

    private fun tryBasicAuth(request: HttpServletRequest) {
        val headerAuth = request.getHeader("Authorization")
        if (headerAuth?.startsWith("Basic") == true) {
            val encoded = headerAuth.removePrefix("Basic").trim()
            try {
                val (user, password) = String(Base64.getDecoder().decode(encoded)).split(":")
                val authentication = authManager.authenticate(UsernamePasswordAuthenticationToken(user, password))
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: IllegalArgumentException) {
                return
            }
        }
    }

    private fun verifyAndGetJwt(request: HttpServletRequest): User? {
        val headerAuth = request.getHeader("Authorization")
        return if (headerAuth?.startsWith(BEARER_PREFIX) == true) {
            val token = headerAuth.removePrefix(BEARER_PREFIX)
            TokenHelper.validateConvertToUser(token)
        } else
            null
    }

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
        const val BEARER_PREFIX = "Bearer "
    }


}