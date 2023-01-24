package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.services.RequestMemoryHandler
import io.rss.openapiboard.server.services.to.MemoryRequestResponse
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Tag(name = "Requests-Memory",
        description = "Endpoints related to handling example/memory requests")
@Path("requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RequestMemoryResource {

    @Inject
    private lateinit var handler: RequestMemoryHandler

    @GET
    @ApiOperation("Listing/Searching request-memories ", notes = "Requires a minimum query (q) according to specified in service")
    fun listAllMemory(@QueryParam("q") query: String?,
                      @QueryParam("pg") @DefaultValue("0") pageIndex: Int) = handler.search(query, pageIndex)

    @POST
    @ApiOperation("Let creating RequestMemory. ", notes = "Id must not be present")
    fun createRequest(@ApiParam request: MemoryRequestResponse): Response {
        return handler.createRequest(request)
                .let { Response.status(Response.Status.CREATED)
                        .entity(it)
                        .build()
                }
    }

    @PUT
    @Path("{id}")
    @ApiOperation("Updating RequestMemory. ", notes = "To update, the id is needed. Tries to create.")
    fun saveRequest(@PathParam("id") id: Long, @ApiParam request: MemoryRequestResponse): MemoryRequestResponse {
        request.requestId = id
        return handler.saveRequest(request)
    }

    @DELETE
    @Path("{rid}")
    @ApiOperation("Removes memory with given Id")
    fun removeRequest(@PathParam("rid") requestId: Long) {
        handler.removeRequest(requestId)
    }
}