package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.persistence.AppVersionDto
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.persistence.entities.AppSnapshotId
import io.rss.openapiboard.server.services.AppRecordHandler
import io.rss.openapiboard.server.services.AppSnapshotHandler
import io.rss.openapiboard.server.services.to.AppComparison
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/** Exposes endpoints to the manager app */

@Tag(name = "Manager",
        description = "Resource for operations from Board's Presentation App")
@Path("manager")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ManagerResource {

    private companion object {
        const val SLASH_REPL = "[[SLASH]]"
    }

    @Inject
    private lateinit var bService: AppRecordHandler

    @Inject
    private lateinit var snapshotService: AppSnapshotHandler

    @Operation(description = "Retrieves the list of existing Namespaces")
    @GET
    @Path("namespaces")
    fun getNamespaces(): List<String> {
        return bService.listNamespaces()
    }

    @Operation(description = "List Apps on the given namespace")
    @GET
    @Path("namespaces/apps")
    fun getAppOnNamespace(@QueryParam("nm") nm: String?): List<AppVersionDto> {
        nm?.let { it ->
            return bService.listAppsByNamespace(decodeUrlPart(it))
        } ?: throw IllegalStateException("Namespace is required to list apps per domain")
    }

    @Operation(description = "Loads the internal App record [namespace + app] without it's source ")
    @GET
    @Path("apps")
    fun loadAppRecord(@QueryParam("nm") nm: String, @QueryParam("app") app: String): AppRecord? {
        return bService.loadAppRecord(AppRecordId(
                decodeUrlPart(app), decodeUrlPart(nm)))
    }

    @Operation(description = "Loads the definition file of the given [namespace + app]")
    @GET
    @Path("source")
    fun loadAppSource(@QueryParam("nm") nm: String, @QueryParam("app") app: String): String? {
        return bService.loadAppRecord(AppRecordId(
                decodeUrlPart(app), decodeUrlPart(nm)))?.source
    }

    @Operation(description = "Retrieves existing versions of apps snapshots")
    @GET
    @Path("versions")
    fun getAppVersionList(@QueryParam("nm") nm: String,
                          @QueryParam("app") app: String): List<String> {
        return snapshotService.listVersionsByAppNamespace(
                decodeUrlPart(app), decodeUrlPart(nm))
    }

    @Operation(description = "Shows oaBoard self open API definitions")
    @GET
    @Path("describe")
    @Produces("text/vnd.yaml")
    fun getSelfAppRecord():String? {
        this.javaClass.getResourceAsStream("/oaboard-api.yaml")
            .use {
                return it.bufferedReader()
                    .readText()
            }
    }

    @Operation(description = "Tries to create a comparison from the 2 versions")
    @POST
    @Path("compare")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun createComparison(@Parameter(description = "Name of 1st app") @FormParam("srcName") srcName: String?,
                         @Parameter(description = "Namespace of 1st app") @FormParam("srcNs") srcNs: String?,
                         @Parameter(description = "Version of 1st app") @FormParam("srcVersion") srcVersion: String?,
                         @Parameter(description = "Name of 2nd app")  @FormParam("compareName") compName: String?,
                         @Parameter(description = "Namespace of 2nd app") @FormParam("compareNs") compNs: String?,
                         @Parameter(description = "Version of 2nd app") @FormParam("compareVersion") compVersion: String?): AppComparison {

        return snapshotService.createComparison(
                AppSnapshotId(
                        srcName ?: throw IllegalArgumentException("Parameter required: sname"),
                        srcNs ?: throw IllegalArgumentException("Parameter required: sns"),
                        srcVersion ?: throw IllegalArgumentException("Parameter required: sver")),
                AppSnapshotId(
                        compName ?: throw IllegalArgumentException("Parameter required: c2name"),
                        compNs ?: throw IllegalArgumentException("Parameter required: c2ns"),
                        compVersion ?: throw IllegalArgumentException("Parameter required: c2ver"))
        )
    }

    private fun decodeUrlPart(txt: String) = txt.replace(SLASH_REPL, "/")
}