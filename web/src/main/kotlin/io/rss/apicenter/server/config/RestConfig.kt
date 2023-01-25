package io.rss.apicenter.server.config

import io.rss.apicenter.server.web.PackageMarker
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.springframework.context.annotation.Configuration
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