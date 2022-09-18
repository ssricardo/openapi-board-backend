package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.persistence.ApiVersionResponse
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import io.rss.openapiboard.server.persistence.entities.ApiSnapshotId
import io.rss.openapiboard.server.services.ApiRecordHandler
import io.rss.openapiboard.server.services.ApiSnapshotHandler
import io.rss.openapiboard.server.services.to.ApiComparisonResponse
import io.rss.openapiboard.server.services.to.ApiRecordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to the manager app */

@Tag(name = "Manager App APIs",
        description = "Resources for operations from Board's Presentation App")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("")
class NamespaceApiResource {

    @Autowired
    private lateinit var bService: ApiRecordHandler

    @Autowired
    private lateinit var snapshotService: ApiSnapshotHandler

    @Operation(description = "Retrieves the list of existing Namespaces")
    @GET
    @Path("namespaces")
    fun getNamespaces(): List<String> {
        return bService.listNamespaces()
    }

    @Operation(description = "List Apis on the given namespace")
    @GET
    @Path("namespaces/{nm}")
    fun getApiOnNamespace(@PathParam("nm") nm: String): List<ApiVersionResponse> {
        return bService.listApiByNamespace(nm)
    }

    @Operation(description = "Loads the internal Api record [namespace + api] without it's source ")
    @GET
    @Path("namespaces/{nm}/apis/{api}")
    fun loadApiRecord(@PathParam("nm") nm: String, @PathParam("api") api: String): ApiRecordResponse? {
        return bService.loadApiRecord(ApiRecordId(api, nm))
                ?.let { ApiRecordResponse(it) }
    }

    @Operation(description = "Loads the definition file of the given [namespace + api]")
    @GET
    @Path("namespaces/{nm}/apis/{api}/source")
    fun loadApiSource(@PathParam("nm") nm: String, @PathParam("api") api: String): String? {
        return bService.loadApiSource(ApiRecordId(
                api, nm))
    }

    @Operation(description = "Retrieves existing versions of apps snapshots")
    @GET
    @Path("namespaces/{nm}/apis/{api}/versions")
    fun getApiVersionList(@PathParam("nm") nm: String,
                          @PathParam("api") app: String): List<String> {
        return snapshotService.listVersionsByApiNamespace(
                app, nm)
    }

    @Operation(description = "Shows oaBoard self open API definitions")
    @GET
    @Path("apis/self")
    @Produces("text/vnd.yaml")
    fun getSelfApiRecord():String {
        this.javaClass.getResourceAsStream("/oaboard-api.yaml")
            .use {
                return it.bufferedReader()
                    .readText()
            }
    }

    @Operation(description = "Tries to create a comparison from the 2 versions")
    @GET
    @Path("apis/comparison")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun createComparison(@Parameter(description = "Name of 1st app") @QueryParam("srcName") srcName: String?,
                         @Parameter(description = "Namespace of 1st app") @QueryParam("srcNs") srcNs: String?,
                         @Parameter(description = "Version of 1st app") @QueryParam("srcVersion") srcVersion: String?,
                         @Parameter(description = "Name of 2nd app")  @QueryParam("compareName") compName: String?,
                         @Parameter(description = "Namespace of 2nd app") @QueryParam("compareNs") compNs: String?,
                         @Parameter(description = "Version of 2nd app") @QueryParam("compareVersion") compVersion: String?): ApiComparisonResponse {

        return snapshotService.buildComparison(
                ApiSnapshotId(
                        srcName ?: throw IllegalArgumentException("Parameter required: srcName"),
                        srcNs ?: throw IllegalArgumentException("Parameter required: srcNs"),
                        srcVersion ?: throw IllegalArgumentException("Parameter required: srcVersion")),
                ApiSnapshotId(
                        compName ?: throw IllegalArgumentException("Parameter required: compareName"),
                        compNs ?: throw IllegalArgumentException("Parameter required: compareNs"),
                        compVersion ?: throw IllegalArgumentException("Parameter required: compareVersion"))
        )
    }

}