package io.rss.apicenter.server.security

object Roles {

    /** For the front-end app */
    const val READER = "READER"

    /** Right to make changes, on front-end */
    const val MANAGER = "MANAGER"

    /** For clients, which feed this app */
    const val AGENT = "AGENT"
}