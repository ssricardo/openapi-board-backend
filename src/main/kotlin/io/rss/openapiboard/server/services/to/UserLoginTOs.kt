package io.rss.openapiboard.server.services.to

/** Groups TOs related to User, Authentication... */

/** Used for logging in */
data class AuthenticationTO(val user: String, val password: String)