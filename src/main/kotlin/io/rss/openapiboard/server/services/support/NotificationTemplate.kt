package io.rss.openapiboard.server.services.support

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Static templating content */
object NotificationTemplate {

    operator fun invoke(date: LocalDateTime, appName: String, unsubscribeLink: String): String {
        return """
            |<p>Dear user, 
            |<br />
            |an update was made in one application which you follow.<br />
            |Namely: $appName <br />
            |When: ${date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} <br />
            |</p>
            |<p>If you want to check out or compare the new version, please access the OpenAPI Board.
            |</p>
            |<br />
            |<div>This is an automatic message. Do not reply to this e-mail</div> 
            |
            |<hr/>
            |<div>
            |If you want to unsubscribe, <a href="$unsubscribeLink" target="_blank">Click here</a>.
            |</div>
        """.trimIndent()
    }
}