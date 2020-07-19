package io.rss.openapiboard.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "env")
data class EnvironmentConfig (
    val serverAddress: String
)
