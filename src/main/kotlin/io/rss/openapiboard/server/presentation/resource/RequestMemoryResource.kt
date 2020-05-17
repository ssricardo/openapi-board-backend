package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.services.SideOperationsProcessor
import io.rss.openapiboard.server.services.to.RequestMemoryInputTO
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.v3.oas.annotations.tags.Tag
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Tag(name = "Requests memory resources",
        description = "Endpoint related to handling sample request")
@Path("app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RequestMemoryResource {

    @Inject
    private lateinit var processor: SideOperationsProcessor

    @GET
    @Path("operations/{namespace}/{app}")
    fun listAppRecordOperations(@PathParam("namespace") namespace: String,
                                @PathParam("app") appName: String)
            = processor.listOperationsByApp(appName, namespace)

    @ApiOperation("Let creating and updating RequestMemory. ",
            notes = "To update, the id is needed. Tries to create.")
    @PUT
    @Path("requests")
    fun saveRequest(@ApiParam request: RequestMemoryInputTO): Response {
        processor.saveRequest(request)
        return Response.ok().build()
    }

    @DELETE
    @Path("requests/{operationId}/{rid}")
    fun removeRequest(@PathParam("operationId") operationId: Int,
                      @PathParam("rid") requestId: Long) {
        processor.removeRequest(operationId, requestId)
    }
}