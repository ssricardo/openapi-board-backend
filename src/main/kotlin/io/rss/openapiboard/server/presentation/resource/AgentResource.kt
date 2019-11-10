package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.AppRecordBusiness
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to partner clients to feed the system */

@Tag(name = "Agent resource",
        description = """Resource that receives API definitions to be registered.
            |Usually should be called from plugins or other tools.""")
@Path("agent")
class AgentResource {

    @Inject
    private lateinit var bService: AppRecordBusiness

    @Operation(description = "Most basic endpoint, to test the connection")
    @GET
    @Path("ping")
    @Produces(MediaType.WILDCARD)
    fun test() = "Pong"

    @Operation(description = "Feeds this application base. Accepts a multipart with data for an AppRegistry.")
    @PUT
    @Path("{namespace}/{name}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun sendAppData(@PathParam("namespace") nm: String, @PathParam("name") name: String,
                    @FormDataParam("file") apiSpec: InputStream,
                    @FormParam("version") versionParam: String,
                    @FormParam("url") url: String) {

        bService.createOrUpdate(AppRecord(name, nm).apply {
            version = versionParam
            address = url
            source = String(apiSpec.readBytes())
        })
    }
}