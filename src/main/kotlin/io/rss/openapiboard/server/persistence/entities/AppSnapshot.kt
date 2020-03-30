package io.rss.openapiboard.server.persistence.entities

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

/** Represents the state of a given app in a namespace in a determined moment
 * Keeps history of an App spec
 * */
@Entity
@IdClass(AppSnapshotId::class)
data class AppSnapshot (

        @Id
        var name: String? = null,

        @Id
        var namespace: String? = null,

        @Id
        @Column(name="version_number", length = 26)
        var version: String? = null

        ): BaseAppData()

data class AppSnapshotId (

        var name: String? = null,

        var namespace: String? = null,

        var version: String? = null

): Serializable