package io.rss.apicenter.server.services.accesscontrol

import io.rss.apicenter.server.persistence.dao.ApiRecordRepository
import io.rss.apicenter.server.persistence.dao.NamespaceCachedRepository
import io.rss.apicenter.server.persistence.dao.RequestSampleRepository
import io.rss.apicenter.server.services.to.RequestSampleTO
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RequestSampleVerifierStrategy(
        private val repository: RequestSampleRepository,
        private val apiRecordRepository: ApiRecordRepository,
        private val namespaceRepository: NamespaceCachedRepository
): TypeVerifierStrategy<RequestSampleTO> {

    override fun getType(): KClass<RequestSampleTO> = RequestSampleTO::class

    override fun hasUserAccess(data: List<RequestSampleTO>): Boolean {
        val namespaceList = apiRecordRepository.findDistinctNamespaces(data.mapNotNull { it.apiId })
        if (!namespaceList.all { hasAccessToNamespace(it, getAuthoritiesString()) }) {
            return false
        }

        val requestIdList = data.mapNotNull(RequestSampleTO::requestId)
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

    override fun filterResultList(data: List<RequestSampleTO>): List<RequestSampleTO> {
        val deniedRequests = repository.findDeniedSamplesForAuthorities(data.mapNotNull { it.requestId },
                getAuthoritiesString())

        return data
            .filter { it.requestId !in deniedRequests }
    }

}