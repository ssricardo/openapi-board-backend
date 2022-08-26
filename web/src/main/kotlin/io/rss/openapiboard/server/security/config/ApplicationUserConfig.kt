package io.rss.openapiboard.server.security.config

import org.springframework.boot.context.properties.ConfigurationProperties

/** Maps list of Users coming from configuration files */
@ConfigurationProperties(prefix = "users")
class UserConfig {
    val entries = mutableListOf<UserEntry>()
}

/** Maps a single user */
data class UserEntry (var name: String? = null) {

    var password: String? = null

    val roles = mutableListOf<String>()
}