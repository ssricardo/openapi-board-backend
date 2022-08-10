package io.rss.openapiboard.server.services.support

import io.rss.openapiboard.server.helper.TokenHelper
import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import io.rss.openapiboard.server.security.Roles
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

/** Handles CRUD operations for AlertSubscription */
@Service
class SubscriptionHandler {

    @Inject
    private lateinit var repository: AlertSubscriptionRepository

    fun find(): List<AlertSubscription> {
        return repository.findAll()
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun saveOrUpdate(@Valid input: AlertSubscription) {
        repository.save(input)
    }

    @Transactional
    fun removeIfVerified(@NotEmpty token: String) {
        TokenHelper.validateRetrieveMailInfo(token).apply {
            repository.findByMailApi(this.email, this.appName)?.let {
                repository.delete(it)
            }
        }
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun removeById(id: Long) {
        repository.findByIdOrNull(id)?.let {
            repository.delete(it)
        } ?: IllegalStateException("No subscription available with given id: $id")
    }

}