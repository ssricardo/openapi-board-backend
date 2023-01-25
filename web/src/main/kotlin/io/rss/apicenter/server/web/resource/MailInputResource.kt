package io.rss.apicenter.server.web.resource

import io.rss.apicenter.server.services.support.SubscriptionHandler
import org.springframework.beans.factory.annotation.Autowired

/** Resource for operations with comes from email, therefore they don't follow same
 * security config
 * */
//@Path("m")
class MailInputResource {

    @Autowired
    lateinit var subscriptionHandler: SubscriptionHandler


}