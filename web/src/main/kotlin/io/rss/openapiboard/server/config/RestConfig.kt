package io.rss.openapiboard.server.config

import io.rss.openapiboard.server.web.PackageMarker
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.jersey.logging.LoggingFeature
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.ServerProperties
import org.springframework.context.annotation.Configuration
import java.util.logging.Level
import java.util.logging.Logger
import javax.ws.rs.ApplicationPath

@Configuration
@ApplicationPath("/")
class RestConfig : ResourceConfig() {

    init {
        packages(PackageMarker::class.java.packageName)
        register(MultiPartFeature::class.java)
        register(JacksonFeature::class.java)
//        register(LoggingFeature::class.java)
    }

}