package io.rss.openapiboard.server.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/** Maps list of Users comming from configuration files */
@ConfigurationProperties(prefix = "users")
@Component
class UserConfig {
    val entries = mutableListOf<UserEntry>()
}

/** Maps a single user */
data class UserEntry (var name: String? = null) {

    var password: String? = null

    val roles = mutableListOf<String>()
}