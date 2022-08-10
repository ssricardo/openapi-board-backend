package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.services.support.SubscriptionHandler
import io.rss.openapiboard.server.services.to.SubscriptionTO
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("subscriptions")
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

    /* @see PATH_UNSUBSCRIBE */
    @Path("unsub-link")
    @GET    // FIXME DELETE BY token
    fun removeSubscription(@QueryParam("tk") token: String?): String {
        handler.removeIfVerified(token
            ?: throw IllegalArgumentException("A token is required through 'tk' parameter")
        )
        return "Subscription removed successfully"
    }

}