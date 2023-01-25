package io.rss.apicenter.server.web.resource

import io.rss.apicenter.server.services.RequestSampleHandler
import io.rss.apicenter.server.services.to.RequestSampleInput
import io.rss.apicenter.server.services.to.RequestSampleResponse
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Tag(name = "Requests-Sample",
        description = "Endpoints related to handling example/sample requests")
@Path("requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RequestSampleResource {

    @Inject
    private lateinit var handler: RequestSampleHandler

    @GET
    @ApiOperation("Listing/Searching request-samples ", notes = "Requires a minimum query (q) according to specified in service")
    fun listAllSamples(@QueryParam("q") query: String?,
                       @QueryParam("pg") @DefaultValue("0") pageIndex: Int) = handler.search(query, pageIndex)

    @POST
    @ApiOperation("Let creating RequestSample. ", notes = "Id must not be present")
    fun createRequest(@ApiParam request: RequestSampleInput): Response {
        return handler.createRequest(request)
                .let { Response.status(Response.Status.CREATED)
                        .entity(it)
                        .build()
                }
    }

    @PUT
    @Path("{id}")
    @ApiOperation("Updating RequestSample. ", notes = "To update, the id is needed. Tries to create.")
    fun saveRequest(@PathParam("id") id: Long, @ApiParam request: RequestSampleInput): RequestSampleResponse {
        request.requestId = id
        return handler.saveRequest(request)
    }

    @DELETE
    @Path("{rid}")
    @ApiOperation("Removes sample with given Id")
    fun removeRequest(@PathParam("rid") requestId: Long) {
        handler.removeRequest(requestId)
    }
}