package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.helper.AuthHelper
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.services.to.AuthenticationTO
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AuthResource {

    @Inject
    private lateinit var authManager: AuthenticationManager

    @POST
    @Path("login")
    @Produces(MediaType.TEXT_PLAIN)
    fun login(@Valid @NotNull request: AuthenticationTO?): String? {
        assertStringRequired(request?.user) {"Username is required for login action"}
        assertStringRequired(request?.password) {"Password is required for login action"}
        request?.let {
            val authentication = authManager.authenticate(UsernamePasswordAuthenticationToken(request.user, request.password))
            SecurityContextHolder.getContext().authentication = authentication
            return AuthHelper.convertToString(authentication)
        }
        return null
    }

}