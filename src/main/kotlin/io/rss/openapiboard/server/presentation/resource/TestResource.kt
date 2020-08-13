package io.rss.openapiboard.server.presentation.resource

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.security.Principal
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Tag(name = "Health", description = "Simple basic resource to use for health check")
@Path("test")
class TestResource {

    @GET
    fun hello(): String {
        return "Hello! Service running... "
    }

    @POST
    @Path("{value}")
    fun helloPost(@PathParam("value") input: String?): String {
        return "Post succeeded for $input. "
    }

    @GET
    @Path("ping")
    fun ping(): String {
        return "pong! "
    }

    @GET
    @Path("user")
    fun roles(): String? {
        val user: Authentication? = SecurityContextHolder.getContext().authentication
        return user?.let {
            println(user.authorities)
            println(user.credentials)
            var springUser = user.principal as User
            springUser.toString()
        } ?: null
    }
}
