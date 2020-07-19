package io.rss.openapiboard.server.services.support

import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import io.rss.openapiboard.server.security.Roles
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

/** Handles CRUD operations for AlertSubscription */
@Service
class SubscriptionHandler {

    // TODO security

    @Inject
    private lateinit var repository: AlertSubscriptionRepository

    fun find(): List<AlertSubscription> {
        return repository.findAll()
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun saveOrUpdate(@Valid input: AlertSubscription) {
        repository.save(input)
    }

    fun removeIfVerified(@NotEmpty token: String) {
        // validate, then delete notif
        repository.deleteByMailApp("ricardo@test.com", "Wasser")
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun removeById(id: Long) {
        repository.deleteById(id)
    }


}