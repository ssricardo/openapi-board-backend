package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.dao.NamespaceCachedRepository
import io.rss.openapiboard.server.persistence.entities.Namespace
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class NamespaceTypeVerifierStrategy(
        val repository: NamespaceCachedRepository
): TypeVerifierStrategy<Namespace> {

    override fun getType(): KClass<Namespace> = Namespace::class

    override fun hasUserAccess(data: List<Namespace>): Boolean {
        val userAuths = getAuthoritiesString()
        val anyNonMatchingAuth = data.asSequence()
                .flatMap { repository.getAuthorities(it.name) ?: listOf() }
                .filter { requiredAuth -> requiredAuth !in userAuths }
                .any()

        return !anyNonMatchingAuth
    }

    override fun filterResultList(data: List<Namespace>): List<Namespace> {
        val userAuths = getAuthoritiesString()

        return data.asSequence()
                .map { Pair(it, repository.getAuthorities(it.name)) }
                .filter { pair -> hasAccessToNamespace(pair.second, userAuths) }
                .map { pair -> pair.first }
                .toList()
    }

    private fun hasAccessToNamespace(requiredAuths: List<String>?, userAuths: List<String>): Boolean {
        if (requiredAuths == null) {
            return true
        }

        return userAuths.containsAll(requiredAuths)
    }

}