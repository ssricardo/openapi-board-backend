package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.services.ApiRecordHandler
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/** Exposes endpoints to partner clients to feed the system */

@Tag(name = "Agent",
        description = """Resource that receives API definitions to be registered.
            |Usually should be called from plugins or other tools.""")
class AgentResource {

    @Inject
    private lateinit var appHandlerService: ApiRecordHandler

    @Operation(description = "Feeds this application base. Accepts a multipart with data for an ApiRegistry.")
    @PUT
    @Path("namespaces/{namespace}/apis/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun sendAppData(@PathParam("namespace") nm: String, @PathParam("name") name: String,
                    @FormDataParam("file") apiSpec: InputStream,
                    @FormDataParam("version") versionParam: String,
                    @FormDataParam("url") url: String) {

        appHandlerService.createOrUpdate(ApiRecord(name, nm).apply {
            version = versionParam
            apiUrl = url
            source = String(apiSpec.readBytes())
        })
    }
}