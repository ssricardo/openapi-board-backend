package io.rss.openapiboard.server.persistence.entities

import java.io.Serializable
import javax.persistence.*

/** Main Entity in the app. Represents the current state of an app, for a given namespace */
@Table(name = "app_record")
@Entity
@IdClass(AppRecordId::class)
data class AppRecord (

        @Id
        var name: String? = null,

        @Id
        var namespace: String? = null): BaseAppData()

@Embeddable
data class AppRecordId (

    var name: String? = null,

    var namespace: String? = null

): Serializable