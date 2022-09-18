package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.services.RequestMemoryHandler
import io.rss.openapiboard.server.services.to.RequestMemoryRequestResponse
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Tag(name = "Requests-Memory",
        description = "Endpoints related to handling example/memory requests")
@Path("requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RequestMemoryResource {

    @Autowired
    private lateinit var handler: RequestMemoryHandler

    @GET
    @ApiOperation("Listing/Searching request-memories ", notes = "Requires a minimum query (q) according to specified in service")
    fun listAllMemory(@QueryParam("q") query: String?,
                      @QueryParam("pg") @DefaultValue("0") pageIndex: Int) = handler.search(query, pageIndex)

    @POST
    @ApiOperation("Let creating RequestMemory. ", notes = "Id must not be present")
    fun createRequest(@ApiParam request: RequestMemoryRequestResponse): Response {
        handler.createRequest(request)
        return Response.status(Response.Status.CREATED).build()
    }

    @PUT
    @Path("{id}")
    @ApiOperation("Updating RequestMemory. ", notes = "To update, the id is needed. Tries to create.")
    fun saveRequest(@PathParam("id") id: Long, @ApiParam request: RequestMemoryRequestResponse) {
        request.requestId = id
        handler.saveRequest(request)
    }

    @DELETE
    @Path("{rid}")
    @ApiOperation("Removes memory with given Id")
    fun removeRequest(@PathParam("rid") requestId: Long) {
        handler.removeRequest(requestId)
    }
}