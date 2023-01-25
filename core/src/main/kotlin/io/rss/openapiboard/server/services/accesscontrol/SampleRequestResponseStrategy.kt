package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.dao.RequestSampleRepository
import io.rss.openapiboard.server.services.NamespaceHandler
import io.rss.openapiboard.server.services.to.SampleRequestResponse
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

//@Component
// TODO remove
class SampleRequestResponseStrategy(
        private val namespaceHandler: NamespaceHandler,
        private val requestSampleRepository: RequestSampleRepository
): TypeVerifierStrategy<SampleRequestResponse> {

    override fun getType(): KClass<SampleRequestResponse> = SampleRequestResponse::class

    override fun filterResultList(data: List<SampleRequestResponse>): List<SampleRequestResponse> {
        val dataWithAllowedNamespace = data.asSequence()
                .filter { it.namespace != null }
                .filter { namespaceHandler.hasUserAccessToNamespace(it.namespace!!) }
                .toList()

        val requestIdList = dataWithAllowedNamespace.mapNotNull(SampleRequestResponse::requestId)
        val deniedSamples = requestSampleRepository.findDeniedSamplesForAuthorities(requestIdList, getAuthoritiesString())

        if (deniedSamples.isEmpty()) {
            return data
        }

        return data.filter {
            it.requestId !in deniedSamples
        }
    }

    override fun hasUserAccess(data: List<SampleRequestResponse>): Boolean {
        val hasAccessToAllNamespaces = data.asSequence()
                .mapNotNull(SampleRequestResponse::namespace)
                .all(namespaceHandler::hasUserAccessToNamespace)

        if (!hasAccessToAllNamespaces) {
            return false
        }

        val requestIdList = data.mapNotNull(SampleRequestResponse::requestId)
        val deniedSamples = requestSampleRepository.findDeniedSamplesForAuthorities(requestIdList, getAuthoritiesString())

        return deniedSamples.isEmpty()
    }

}