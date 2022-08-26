package io.rss.openapiboard.server.services.to

/** Groups TOs of support operations, to prepared to be exposed on the view */

/** Used for logging in */
data class AuthenticationTO(val user: String, val password: String)

