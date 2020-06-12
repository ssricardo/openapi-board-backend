package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.RequestMemoryHandler
import io.rss.openapiboard.server.services.AppSourceProcessor
import io.rss.openapiboard.server.services.to.RequestMemoryViewTO
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Tag(name = "Memory",
        description = "Endpoints related to handling example/memory requests")
@Path("app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RequestMemoryResource {

    @Inject
    private lateinit var processor: AppSourceProcessor

    @Inject
    private lateinit var handler: RequestMemoryHandler

    @GET
    @Path("requests")
    @ApiOperation("Listing/Searching request-memories ", notes = "Requires a minimum query (q) according to specified in service")
    fun listAllMemory(@QueryParam("q") query: String?,
                      @QueryParam("pg") @DefaultValue("0") pageIndex: Int)
        = handler.search(query, pageIndex)

    @PUT
    @Path("requests")
    @ApiOperation("Let creating and updating RequestMemory. ", notes = "To update, the id is needed. Tries to create.")
    fun saveRequest(@ApiParam request: RequestMemoryViewTO): Response {
        handler.saveRequest(request)
        return Response.ok().build()
    }

    @DELETE
    @Path("requests/{rid}")
    @ApiOperation("Removes memory with given Id")
    fun removeRequest(@PathParam("rid") requestId: Long) {
        handler.removeRequest(requestId)
    }
}