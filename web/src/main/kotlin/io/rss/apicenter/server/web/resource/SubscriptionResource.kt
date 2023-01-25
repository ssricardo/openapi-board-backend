package io.rss.apicenter.server.web.resource

import io.rss.apicenter.server.services.support.SubscriptionHandler
import io.rss.apicenter.server.services.to.SubscriptionRequestResponse
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
    fun getSubscribers(): List<SubscriptionRequestResponse> =
        handler.find().map {
            SubscriptionRequestResponse(it)
        }

    @POST
    fun create(value: SubscriptionRequestResponse): Response {
        handler.addSubscription(value.unwrap())
        return Response.status(Response.Status.CREATED).build()
    }

    @Path("{id}")
    @PUT
    fun update(@PathParam("id") id: Long, value: SubscriptionRequestResponse) {
        value.id = id
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