package io.rss.apicenter.server.web.resource

import io.rss.apicenter.server.persistence.entities.ApiSnapshotId
import io.rss.apicenter.server.persistence.entities.Namespace
import io.rss.apicenter.server.services.ApiRecordHandler
import io.rss.apicenter.server.services.ApiSnapshotHandler
import io.rss.apicenter.server.services.NamespaceHandler
import io.rss.apicenter.server.services.to.ApiComparisonResponse
import io.rss.apicenter.server.services.to.ApiRecordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to the manager app */

@Tag(name = "Manager App APIs",
        description = "Resources for operations from API Center's Presentation App")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("apis")
class ApiRecordResource {

    @Inject
    private lateinit var namespaceHandler: NamespaceHandler

    @Inject
    private lateinit var apiHandler: ApiRecordHandler

    @Inject
    private lateinit var snapshotHandler: ApiSnapshotHandler

    @Operation(description = "List Apis on the given namespace")
    @GET
    fun getApiOnNamespace(@QueryParam("nm") nm: String?) =
            nm?.let { apiHandler.listApiByNamespace(Namespace(nm)) }

    @Operation(description = "Loads the internal Api record [namespace + api] without it's source ")
    @GET
    @Path("{api}")
    fun loadApiRecord(@PathParam("api") apiId: UUID?): ApiRecordResponse? {
//        namespaceHandler.assertUserHasAccess(nm)
        return apiId?.let {
            apiHandler.loadApiRecord(apiId)?.let {
                ApiRecordResponse(it) }
        }
    }

    @Operation(description = "Loads the definition file of the given [namespace + api]")
    @GET
    @Path("{api}/source")
    fun loadApiSource(@PathParam("api") apiId: UUID): String? {
        return apiHandler.loadApiSource(apiId)
    }

    @Operation(description = "Retrieves existing versions of apps snapshots")
    @GET
    @Path("{api}/versions")
    fun getApiVersionList(@PathParam("api") apiId: UUID): List<String> {
        return snapshotHandler.listVersionsByApi(apiId)
    }

    @Operation(description = "Shows API Center self open API definitions")
    @GET
    @Path("apis/self")
    @Produces("text/vnd.yaml")
    fun getSelfApiRecord():String? {
        return this.javaClass.getResourceAsStream("/oaboard-api.yaml")
            ?.use {
                it.bufferedReader()
                    .readText()
            }
    }

    @Operation(description = "Tries to create a comparison from the 2 versions")
    @GET
    @Path("comparison")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun createComparison(@Parameter(description = "Name of 1st app") @QueryParam("srcName") srcName: String?,
                         @Parameter(description = "Namespace of 1st app") @QueryParam("srcNs") srcNs: String?,
                         @Parameter(description = "Version of 1st app") @QueryParam("srcVersion") srcVersion: String?,
                         @Parameter(description = "Namespace of 2nd app") @QueryParam("compareNs") compNs: String?,
                         @Parameter(description = "Version of 2nd app") @QueryParam("compareVersion") compVersion: String?): ApiComparisonResponse {

        namespaceHandler.assertUserHasAccess(srcNs ?: throw IllegalArgumentException("Parameter required: srcNs"))
        namespaceHandler.assertUserHasAccess(compNs ?: throw IllegalArgumentException("Parameter required: compareNs"))

        return snapshotHandler.buildComparison(
                ApiSnapshotId(
                        srcName ?: throw IllegalArgumentException("Parameter required: srcName"),
                        srcNs,
                        srcVersion ?: throw IllegalArgumentException("Parameter required: srcVersion")),
                ApiSnapshotId(
                        srcName,
                        compNs,
                        compVersion ?: throw IllegalArgumentException("Parameter required: compareVersion"))
        )
    }

}