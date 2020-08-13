package io.rss.openapiboard.server.config

import org.glassfish.jersey.logging.LoggingFeature
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
        register(LoggingFeature::class.java)
    }

}