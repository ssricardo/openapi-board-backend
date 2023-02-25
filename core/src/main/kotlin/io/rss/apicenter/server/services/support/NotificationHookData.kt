package io.rss.apicenter.server.services.support

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



internal data class NotificationHookData (val apiName: String,
                                          val namespace: String,
                                          val newVersion: String,
                                          val oldVersion: String?,
                                          val changeDate: LocalDateTime) {

}