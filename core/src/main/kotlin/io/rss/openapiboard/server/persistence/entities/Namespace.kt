package io.rss.openapiboard.server.persistence.entities

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Entity
@Table(name = "namespaces")
class Namespace (

    @Id
    @Column(length = 50)
    @field:NotEmpty
    @field:Size(min = 2, max = 50)
    @field:Pattern(regexp = "\\w(\\w|-|\\.)*\\w", message = "Invalid namespace name")
    val name: String
) {

    @OneToMany(mappedBy = "namespace", cascade = [CascadeType.ALL], orphanRemoval = true)
    var requiredAuthorities: List<NamespaceAuthority> = listOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Namespace

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}