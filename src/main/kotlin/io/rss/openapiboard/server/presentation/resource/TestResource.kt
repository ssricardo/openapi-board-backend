package io.rss.openapiboard.server.presentation.resource

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.security.Principal
import javax.ws.rs.GET
import javax.ws.rs.Path

@Tag(name = "Health resource", description = "Simple basic resource to use for health check")
@Path("test")
class TestResource {

    @GET
    fun hello(): String {
        return "Hello! Service running..."
    }

    @GET
    @Path("ping")
    fun ping(): String {
        return "pong"
    }

    @GET
    @Path("user")
    fun roles(): String {
        val user = SecurityContextHolder.getContext().authentication
        println(user.authorities)
        println(user.credentials)
        var springUser = user.principal as User
        return springUser.toString()
    }
}
