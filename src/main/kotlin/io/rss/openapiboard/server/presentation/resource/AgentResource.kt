package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.AppRecordHandler
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/** Exposes endpoints to partner clients to feed the system */

@Tag(name = "Agent resource",
        description = """Resource that receives API definitions to be registered.
            |Usually should be called from plugins or other tools.""")
@Path("agent")
class AgentResource {

    @Inject
    private lateinit var appHandlerService: AppRecordHandler

    @Operation(description = "Most basic endpoint, to test the connection")
    @GET
    @Path("ping")
    @Produces(MediaType.WILDCARD)
    fun test() = "Pong"

    @Operation(description = "Feeds this application base. Accepts a multipart with data for an AppRegistry.")
    @PUT
    @Path("{namespace}/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun sendAppData(@PathParam("namespace") nm: String, @PathParam("name") name: String,
                    @FormDataParam("file") apiSpec: InputStream,
                    @FormDataParam("version") versionParam: String,
                    @FormDataParam("url") url: String): Response {

        appHandlerService.createOrUpdate(AppRecord(name, nm).apply {
            version = versionParam
            address = url
            source = String(apiSpec.readBytes())
        })

        return Response.ok().build()
    }
}