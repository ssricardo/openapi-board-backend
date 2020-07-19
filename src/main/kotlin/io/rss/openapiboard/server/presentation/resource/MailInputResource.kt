package io.rss.openapiboard.server.presentation.resource

import io.rss.openapiboard.server.services.support.SubscriptionHandler
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

/** Resource for operations with comes from email, therefore they don't follow same
 * security config
 * */
@Path("m")
class MailInputResource {

    @Inject
    lateinit var subscriptionHandler: SubscriptionHandler

    /* @see PATH_UNSUBSCRIBE */
    @Path("rem-subs")
    @GET
    fun removeSubscription(@QueryParam("tk") token: String?): String {
        subscriptionHandler.removeIfVerified(token
                ?: throw IllegalArgumentException("A token is required through 'tk' parameter"))
        return "Subscription removed successfully"
    }
}