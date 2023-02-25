package io.rss.apicenter.server.services.support

import io.rss.apicenter.server.helper.TokenHelper
import io.rss.apicenter.server.helper.assertGetStringsRequired
import io.rss.apicenter.server.persistence.dao.ApiSubscriptionRepository
import io.rss.apicenter.server.persistence.entities.ApiSubscription
import io.rss.apicenter.server.security.Roles
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Service
@Validated
class SubscriptionHandler (private val repository: ApiSubscriptionRepository) {

    fun listAll(): List<ApiSubscription> =
        repository.findAll()

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @Transactional
    fun addSubscription(@Valid subscription: ApiSubscription): ApiSubscription {
        require(subscription.id == null) { "New subscriptions must not have an id" }
        return saveOrUpdate(subscription)
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @Transactional
    fun saveOrUpdate(@Valid input: ApiSubscription): ApiSubscription {
        val (webhook, apiName) = assertGetStringsRequired({ "Webhook address and API are mandatory" }, input.targetWebhook, input.apiName)

        val existingSubscription = repository.findByHookApi(webhook, apiName)
            ?.let {
                it.basePaths = input.basePaths
                it
            }

        return existingSubscription
            ?: repository.save(input)
    }

    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun removeById(id: Long) {
        repository.findByIdOrNull(id)?.let {
            repository.delete(it)
        } ?: throw IllegalArgumentException("No subscription available with given id: $id")
    }

}

