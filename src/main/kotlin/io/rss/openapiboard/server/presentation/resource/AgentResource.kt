package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.AppRecordBusiness
import io.rss.openapiboard.server.persistence.entities.AppRecord
import org.glassfish.jersey.media.multipart.FormDataParam
import java.io.InputStream
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to partner clients to feed the system */

@Path("agent")
class AgentResource {

    @Inject
    private lateinit var bService: AppRecordBusiness

    @GET
    fun test() = "Hello Ricardo"    // TODO remove

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