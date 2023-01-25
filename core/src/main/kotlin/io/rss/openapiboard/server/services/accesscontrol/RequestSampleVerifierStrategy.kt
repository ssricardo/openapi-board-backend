package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.dao.NamespaceCachedRepository
import io.rss.openapiboard.server.persistence.dao.RequestSampleRepository
import io.rss.openapiboard.server.services.to.SampleRequestResponse
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RequestSampleVerifierStrategy(
        private val repository: RequestSampleRepository,
        private val namespaceRepository: NamespaceCachedRepository
): TypeVerifierStrategy<SampleRequestResponse> {

    override fun getType(): KClass<SampleRequestResponse> = SampleRequestResponse::class

    override fun hasUserAccess(data: List<SampleRequestResponse>): Boolean {
        val userAuthorities = getAuthoritiesString()
        val hasAccessToAllNamespaces = data.asSequence().mapNotNull { it.namespace }.all { hasAccessToNamespace(it, userAuthorities) }

        if (!hasAccessToAllNamespaces) {
            return false
        }

        val requestIdList = data.mapNotNull(SampleRequestResponse::requestId)
        val deniedSamples = repository.findDeniedSamplesForAuthorities(requestIdList, getAuthoritiesString())

        return deniedSamples.isEmpty()
    }

    private fun hasAccessToNamespace(namespace: String, userAuthorities: List<String>): Boolean {
        val requiredAuthorities = namespaceRepository.getAuthorities(namespace)
        if (requiredAuthorities.isNullOrEmpty()) {
            return true
        }

        return requiredAuthorities
                .any { userAuthorities.contains(it) }
    }

    override fun filterResultList(data: List<SampleRequestResponse>): List<SampleRequestResponse> {
        val deniedRequests = repository.findDeniedSamplesForAuthorities(data.mapNotNull { it.requestId },
                getAuthoritiesString())

        return data.filter { it.requestId !in deniedRequests }
    }

}