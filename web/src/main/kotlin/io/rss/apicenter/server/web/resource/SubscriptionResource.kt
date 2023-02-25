package io.rss.apicenter.server.web.resource

import io.rss.apicenter.server.persistence.entities.ApiSubscription
import io.rss.apicenter.server.services.support.SubscriptionHandler
import io.rss.apicenter.server.services.to.SubscriptionRequest
import io.rss.apicenter.server.services.to.SubscriptionRequestResponse
import io.rss.apicenter.server.services.to.SubscriptionResponse
import io.rss.apicenter.server.services.to.toRequestResponseTO
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SubscriptionResource {

    @Inject
    private lateinit var handler: SubscriptionHandler

    @GET
    fun getSubscribers(): List<SubscriptionResponse> =
        handler.listAll().map (ApiSubscription::toRequestResponseTO)

    @POST
    fun create(value: SubscriptionRequest): Response {
        return handler.addSubscription(value.toApiSubscription())
            .let { result ->
                Response.status(Response.Status.CREATED)
                    .entity(result)
                    .build()
            }
    }

    @Path("{id}")
    @PUT
    fun update(@PathParam("id") id: Long, value: SubscriptionRequest): Response {
        value.id = id
        handler.saveOrUpdate(value.toApiSubscription())
        return Response.ok().build();
    }

    @Path("{id}")
    @DELETE
    fun removeSubscription(@PathParam("id") id: Long) {
        handler.removeById(id)
    }

}