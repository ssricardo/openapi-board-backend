package io.rss.openapiboard.server.services.support

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Static templating content */
internal object NotificationTemplate {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")

    operator fun invoke(date: LocalDateTime, appName: String, newVersion: String, unsubscribeLink: String): String {
        return """
            |<p>Dear user, 
            |<br />
            |an update was made in one API which you follow.<br /><br />
            |<b>API:</b> $appName <br />
            |<b>When:</b> ${date.format(dateFormat)} <br />
            |<b>New version:</b> $newVersion <br />
            |</p>
            |<p>If you want to check out or compare the new version, please access the OpenAPI Center.
            |</p>
            |<br />
            |<div>This is an automatic message. Do not reply to this e-mail</div> 
            |
            |<hr/>
            |<div>
            |If you want to unsubscribe, <a href="$unsubscribeLink" target="_blank">Click here</a>.
            |</div>
        """.trimMargin("|")
    }
}