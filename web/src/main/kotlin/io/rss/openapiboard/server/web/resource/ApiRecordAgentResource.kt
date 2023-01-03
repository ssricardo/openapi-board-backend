package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.services.ApiRecordHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to partner clients to feed the system */

@Tag(name = "Agent APIs",
        description = """Resource that receives API definitions to be registered.
            |Usually should be called from plugins or other tools.""")
@Path("")
@Produces(MediaType.APPLICATION_JSON)
class ApiRecordAgentResource {

    @Inject
    private lateinit var apiHandlerService: ApiRecordHandler

    @Operation(description = "Feeds this application base. Accepts a multipart with data for an ApiRegistry.")
    @PUT
    @Path("namespaces/{namespace}/apis/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun sendAppData(@PathParam("name") name: String,
                    @PathParam("namespace") nm: String,
                    @FormDataParam("file") apiSpec: InputStream,
                    @FormDataParam("version") versionParam: String,
                    @FormDataParam("url") url: String): UUID? {

        return apiHandlerService.createOrUpdate(ApiRecord(name, nm, versionParam).apply {
            apiUrl = url
            source = String(apiSpec.readBytes())
        }).id
    }

}