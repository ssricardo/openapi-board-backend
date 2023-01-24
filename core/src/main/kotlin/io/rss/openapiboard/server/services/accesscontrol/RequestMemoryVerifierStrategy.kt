package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.dao.NamespaceCachedRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.services.to.MemoryRequestResponse
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RequestMemoryVerifierStrategy(
        val repository: RequestMemoryRepository,
        val namespaceRepository: NamespaceCachedRepository
): TypeVerifierStrategy<MemoryRequestResponse> {

    override fun getType(): KClass<MemoryRequestResponse> = MemoryRequestResponse::class

    override fun hasUserAccess(data: List<MemoryRequestResponse>): Boolean {
        val userAuthorities = getAuthoritiesString()
        return data.asSequence().mapNotNull { it.namespace }.all { hasAccessToNamespace(it, userAuthorities) }
    }

    private fun hasAccessToNamespace(namespace: String, userAuthorities: List<String>): Boolean {
        val requiredAuthorities = namespaceRepository.getAuthorities(namespace)
        if (requiredAuthorities.isNullOrEmpty()) {
            return true
        }

        return requiredAuthorities
                .any { userAuthorities.contains(it) }
    }

    override fun filterResultList(data: List<MemoryRequestResponse>): List<MemoryRequestResponse> {
        val deniedRequests = repository.findDeniedMemoriesForAuthorities(data.mapNotNull { it.requestId },
                getAuthoritiesString())

        return data.filter { it.requestId !in deniedRequests }
    }

}