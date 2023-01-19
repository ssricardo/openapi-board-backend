package io.rss.openapiboard.server.services.to

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/** Groups TOs of support operations, to prepared to be exposed on the view */

/** Used for logging in */
data class AuthenticationTO(val user: String, val password: String)

data class NamespaceViewTO(@field:NotNull @field:NotEmpty val name: String?,
                           var authorities: List<String> = listOf())