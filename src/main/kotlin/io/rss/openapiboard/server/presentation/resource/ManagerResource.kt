package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.services.AppRecordBusiness
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to the manager app */

@Tag(name = "Manager Resources",
        description = "Resource for operations from Board's Presentation App")
@Path("manager")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ManagerResource {

    @Inject
    private lateinit var bService: AppRecordBusiness


    @Operation(description = "Retrieves the list of existing Namespaces")
    @GET
    @Path("namespaces")
    fun getNamespaces(): List<String> {
        return bService.listNamespaces()
    }

    @Operation(description = "List Apps on the given namespace")
    @GET
    @Path("{namespace}")
    fun getAppOnNamespace(@PathParam("namespace") nm: String?): List<String> {
        nm?.let {
            return bService.listNamesByNamespace(it)
        } ?: throw IllegalStateException("Namespace is required to list apps per domain")
    }

    @Operation(description = "Loads the definition file of the given [namespace + app]")
    @GET
    @Path("{namespace}/{app}")
    fun loadAppRecord(@PathParam("namespace") nm: String, @PathParam("app") app: String): AppRecord? {
        return bService.loadAppRecord(AppRecordId(app, nm))
    }

    @Operation(description = "Temporary endpoint")
    @GET
    @Path("describe")
    @Produces("text/vnd.yaml")
    fun getSelfAppRecord():String? {
        Files.newInputStream(
            Paths.get("D:\\dev\\git\\openapi-center\\openapi-board-server\\build\\swagger\\openapi.yaml"))
            .use {
                return it.bufferedReader()
                    .readText()
            }
    }
}