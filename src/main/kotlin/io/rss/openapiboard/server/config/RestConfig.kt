package io.rss.openapiboard.server.config

import io.rss.openapiboard.server.presentation.resource.AgentResource
import io.rss.openapiboard.server.presentation.resource.TestResource
import io.rss.openapiboard.server.presentation.resource.ManagerResource
import io.rss.openapiboard.server.presentation.resource.RequestMemoryResource
//import io.rss.openapiboard.server.presentation.resource.TestResource
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.springframework.context.annotation.Configuration
import javax.ws.rs.ApplicationPath

@Configuration
@ApplicationPath("/")
class RestConfig : ResourceConfig() {

    init {
        packages("io.rss.openapiboard.server.presentation")
        register(MultiPartFeature::class.java)
        register(TestResource::class.java)
        register(ManagerResource::class.java)
        register(AgentResource::class.java)
        register(RequestMemoryResource::class.java)
    }

}