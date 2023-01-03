package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.NamespaceCachedRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.Namespace
import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.services.accesscontrol.AssertRequiredAuthorities
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.function.Supplier

@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
class NamespaceHandler (
    private val namespaceRepository: NamespaceCachedRepository,
    private val authoritiesProvider: Supplier<Collection<GrantedAuthority>>
) {

    @Autowired
    constructor(namespaceRepository: NamespaceCachedRepository) : this(namespaceRepository,
            Supplier { SecurityContextHolder.getContext().authentication.authorities })

    fun assertUserHasAccess(namespace: String) {
        if (!namespaceRepository.exists(namespace)) {
            throw IllegalArgumentException("Namespace not found: $namespace")
        }

        val userAuths = authoritiesProvider.get().map (GrantedAuthority::getAuthority)
        if (!hasUserAccessToNamespace(namespace, userAuths)) {
            throw AccessDeniedException("Current user doesn't have access to requested namespace")
        }
    }

    @AssertRequiredAuthorities
    fun saveNamespace(ns: Namespace, requiredAuthorities: List<String>): Namespace {
        // TODO: something with requiredAuthorities
        return namespaceRepository.saveOrUpdate(ns)
    }

    fun removeNamespace(name: String) {
        namespaceRepository.remove(name)
        // TODO("What to do if there are APIs in the namespace?")
    }

    // TODO not used/remove?
    fun filterAllowedApi(apiList: List<ApiRecord>): List<ApiRecord> {
        val userAuths = authoritiesProvider.get().map (GrantedAuthority::getAuthority)
        return apiList
                .filter { api ->
                    hasUserAccessToNamespace(api.namespace, userAuths)
                }
    }

    private fun hasUserAccessToNamespace(namespaceId: String, userAuths: List<String>): Boolean {
        val nsRequirement = namespaceRepository.getAuthorities(namespaceId)
                ?: return warnNamespaceNotExists(namespaceId)

        if (nsRequirement.isEmpty()) {
            return true
        }

        return userAuths.any { userAuth -> userAuth in nsRequirement }
    }

    fun hasUserAccessToNamespace(namespaceId: String): Boolean {
        val userAuths = authoritiesProvider.get().map (GrantedAuthority::getAuthority)
        return hasUserAccessToNamespace(namespaceId, userAuths)
    }

    private fun warnNamespaceNotExists(namespaceId: String): Boolean {
        LOG.warn("A namespace was not found but its not expected in this point: $namespaceId")
        return false
    }

    fun listNamespaces(): List<String> {
        val userAuths = authoritiesProvider.get().map(GrantedAuthority::getAuthority)
        return namespaceRepository.findAll()
                .filter { hasUserAccessToNamespace(it, userAuths) }
    }

    fun exists(namespaceId: String) = namespaceRepository.exists(namespaceId)

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(NamespaceHandler::class.java)
    }
}