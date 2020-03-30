package io.rss.openapiboard.server.persistence.entities

import java.io.Serializable
import javax.persistence.*

/** Main Entity in the app. Represents the current state of an app, for a given namespace */
@Table(name = "app_record")
@Entity
@IdClass(AppRecordId::class)
data class AppRecord(

        @Id
        var name: String? = null,

        @Id
        var namespace: String? = null) : BaseAppData() {

        @Column(name = "app_version", length = 30)
        var version: String? = null

        @Column(name = "app_path", length = 200)
        var path: String? = null
}

@Embeddable
data class AppRecordId (

    var name: String? = null,

    var namespace: String? = null

): Serializable