package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.persistence.ApiVersionDto
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import io.rss.openapiboard.server.persistence.entities.ApiSnapshotId
import io.rss.openapiboard.server.services.ApiRecordHandler
import io.rss.openapiboard.server.services.ApiSnapshotHandler
import io.rss.openapiboard.server.services.to.ApiComparisonResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to the manager app */

@Tag(name = "Manager",
        description = "Resources for operations from Board's Presentation App")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ManagerResource {

    @Inject
    private lateinit var bService: ApiRecordHandler

    @Inject
    private lateinit var snapshotService: ApiSnapshotHandler

    @Operation(description = "Retrieves the list of existing Namespaces")
    @GET
    @Path("namespaces")
    fun getNamespaces(): List<String> {
        return bService.listNamespaces()
    }

    @Operation(description = "List Apps on the given namespace")
    @GET
    @Path("namespaces/{nm}/apis")
    fun getApiOnNamespace(@PathParam("nm") nm: String): List<ApiVersionDto> {
        return bService.listApiByNamespace(decodeUrlPart(nm))
    }

    @Operation(description = "Loads the internal App record [namespace + app] without it's source ")
    @GET
    @Path("namespaces/{nm}/apis/{api}")
    fun loadApiRecord(@PathParam("nm") nm: String, @PathParam("api") api: String): ApiRecord? {
        return bService.loadApiRecord(
            ApiRecordId(decodeUrlPart(api), decodeUrlPart(nm)))
    }

    @Operation(description = "Loads the definition file of the given [namespace + app]")
    @GET
    @Path("namespaces/{nm}/apis/{api}/source")
    fun loadApiSource(@PathParam("nm") nm: String, @PathParam("api") api: String): String? {
        return bService.loadApiSource(ApiRecordId(
                decodeUrlPart(api), decodeUrlPart(nm)))
    }

    @Operation(description = "Retrieves existing versions of apps snapshots")
    @GET
    @Path("namespaces/{nm}/apis/{api}/versions")
    fun getApiVersionList(@PathParam("nm") nm: String,
                          @PathParam("api") app: String): List<String> {
        return snapshotService.listVersionsByApiNamespace(
                decodeUrlPart(app), decodeUrlPart(nm))
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
    @POST
    @Path("apis/comparison")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun createComparison(@Parameter(description = "Name of 1st app") @QueryParam("srcName") srcName: String?,
                         @Parameter(description = "Namespace of 1st app") @QueryParam("srcNs") srcNs: String?,
                         @Parameter(description = "Version of 1st app") @QueryParam("srcVersion") srcVersion: String?,
                         @Parameter(description = "Name of 2nd app")  @QueryParam("compareName") compName: String?,
                         @Parameter(description = "Namespace of 2nd app") @QueryParam("compareNs") compNs: String?,
                         @Parameter(description = "Version of 2nd app") @QueryParam("compareVersion") compVersion: String?): ApiComparisonResponse {

        return snapshotService.createComparison(
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

    private fun decodeUrlPart(txt: String) = txt.replace(SLASH_REPL, "/")

    private companion object {
        private const val SLASH_REPL = "[[SLASH]]"
    }

}