package io.rss.openapiboard.server.config

import io.rss.openapiboard.server.presentation.resource.AgentResource
import io.rss.openapiboard.server.presentation.resource.ManagerResource
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.springframework.context.annotation.Configuration
import javax.ws.rs.ApplicationPath

@Configuration
@ApplicationPath("api")
class RestConfig : ResourceConfig() {

    init {
        packages("io.rss.openapiboard.server.presentation")
        register(MultiPartFeature::class.java)
        register(ManagerResource::class.java)
        register(AgentResource::class.java)
    }

}