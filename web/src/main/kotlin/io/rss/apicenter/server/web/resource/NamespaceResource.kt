package io.rss.apicenter.server.web.resource

import io.rss.apicenter.server.persistence.entities.Namespace
import io.rss.apicenter.server.services.NamespaceHandler
import io.rss.apicenter.server.services.to.NamespaceViewTO
import io.rss.apicenter.server.services.to.toViewTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Tag(name = "Manager App APIs",
        description = "Resources for operations from API Center's Presentation App")
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
                .toViewTO()

    @PUT
    fun createOrUpdateNs(@NotNull input: NamespaceViewTO) =
            namespaceHandler.saveNamespace(
                    Namespace(input.name ?: throw IllegalArgumentException("Name is mandatory")),
                    input.authorities)
                .toViewTO()

    @DELETE
    @Path("{nsId}")
    fun removeNs(@PathParam("nsId") nsId: String) =
            namespaceHandler.removeNamespace(nsId)

}