package io.rss.apicenter.server.persistence.entities

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

/** Represents the state of a given api in a namespace in a determined moment
 * Keeps history of an App spec
 * */
@Entity
@IdClass(ApiSnapshotId::class)
data class ApiSnapshot (

        @Id
        var name: String,

        @Id
        var namespace: String,

        @Id
        @Column(name="version_number", length = 26)
        var version: String

        ): BaseApiData()

data class ApiSnapshotId (

        var name: String? = null,

        var namespace: String? = null,

        var version: String? = null

): Serializable