package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.persistence.entities.Namespace
import io.rss.openapiboard.server.services.NamespaceHandler
import io.rss.openapiboard.server.services.to.NamespaceViewTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Tag(name = "Manager App APIs",
        description = "Resources for operations from Board's Presentation App")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("namespaces")
class NamespaceResource {

    @Inject
    private lateinit var namespaceHandler: NamespaceHandler

    @Operation(description = "Retrieves the list of existing Namespaces")
    @GET
    fun getNamespaces(): List<String> =
            namespaceHandler.listNamespaces()

    @POST
    fun createNs(@NotNull input: NamespaceViewTO) =
            namespaceHandler.saveNamespace(
                    Namespace(input.name ?: throw IllegalArgumentException("Name is mandatory")),
                    input.authorities)

    @PUT
    fun createOrUpdateNs(@NotNull input: NamespaceViewTO) =
            namespaceHandler.saveNamespace(
                    Namespace(input.name ?: throw IllegalArgumentException("Name is mandatory")),
                    input.authorities)

    @DELETE
    @Path("{nsId}")
    fun removeNs(@PathParam("nsId") nsId: String) =
            namespaceHandler.removeNamespace(nsId)

}