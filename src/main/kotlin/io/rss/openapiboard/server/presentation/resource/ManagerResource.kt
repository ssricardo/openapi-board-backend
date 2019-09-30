package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.AppRecordBusiness
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to the manager app */

@Path("manager")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ManagerResource {

    @Inject
    private lateinit var bService: AppRecordBusiness

    @GET
    @Path("namespaces")
    fun getNamespaces(): List<String> {
        return bService.listNamespaces()
    }

    @GET
    @Path("{namespace}")
    fun getAppOnNamespace(@PathParam("namespace") nm: String?): List<String> {
        nm?.let {
            return bService.listNamesByNamespace(it)
        } ?: throw IllegalStateException("Namespace is required to list apps per domain")
    }

    @GET
    @Path("{namespace}/{app}")
    fun loadAppRecord(@PathParam("namespace") nm: String, @PathParam("app") app: String): AppRecord? {
        return bService.loadAppRecord(AppRecordId(app, nm))
    }
}