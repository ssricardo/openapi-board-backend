package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.support.SubscriptionHandler
import io.rss.openapiboard.server.services.to.SubscriptionTO
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("subscription")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SubscriptionResource {

    @Inject
    private lateinit var handler: SubscriptionHandler

    @GET
    fun getSubscribers(): List<SubscriptionTO> =
        handler.find().map {
            SubscriptionTO(it)
        }

    @PUT
    fun store(value: SubscriptionTO) {
        handler.saveOrUpdate(value.unwrap())
    }

    @Path("{id}")
    @DELETE
    fun removeSubscription(@PathParam("id") id: Long) {
        handler.removeById(id)
    }

}