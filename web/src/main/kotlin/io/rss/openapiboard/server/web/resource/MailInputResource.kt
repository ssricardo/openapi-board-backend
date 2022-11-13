package io.rss.openapiboard.server.web.resource

import io.rss.openapiboard.server.services.support.SubscriptionHandler
import org.springframework.beans.factory.annotation.Autowired
import java.lang.IllegalArgumentException
import javax.annotation.Resource
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

/** Resource for operations with comes from email, therefore they don't follow same
 * security config
 * */
//@Path("m")
class MailInputResource {

    @Autowired
    lateinit var subscriptionHandler: SubscriptionHandler


}