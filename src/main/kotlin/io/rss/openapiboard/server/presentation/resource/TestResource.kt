package io.rss.openapiboard.server.presentation.resource

import javax.ws.rs.GET
import javax.ws.rs.Path

import io.swagger.v3.oas.annotations.tags.Tag

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
}
