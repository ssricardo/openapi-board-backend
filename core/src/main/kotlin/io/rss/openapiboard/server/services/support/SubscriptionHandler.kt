package io.rss.openapiboard.server.services.support

import io.rss.openapiboard.server.helper.TokenHelper
import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import io.rss.openapiboard.server.security.Roles
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

/** Handles CRUD operations for AlertSubscription */
@Service
class SubscriptionHandler (private val repository: AlertSubscriptionRepository) {

    fun find(): List<AlertSubscription> {
        return repository.findAll()
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @Transactional
    fun addSubscription(subscription: AlertSubscription): AlertSubscription {
        require(subscription.id == null) { "New subscriptions must not have an id" }
        return saveOrUpdate(subscription)
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @Transactional
    fun saveOrUpdate(@Valid input: AlertSubscription): AlertSubscription =
        repository.save(input)

    @Transactional
    fun removeIfVerified(@NotEmpty token: String) {
        val subscriptionId = TokenHelper.validateRetrieveMailInfo(token)

        repository.findByMailApi(subscriptionId.email, subscriptionId.appName)?.let {
            repository.delete(it)
        }
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun removeById(id: Long) {
        repository.findByIdOrNull(id)?.let {
            repository.delete(it)
        } ?: throw IllegalArgumentException("No subscription available with given id: $id")
    }

}