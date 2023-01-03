package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.services.NamespaceHandler
import io.rss.openapiboard.server.services.to.MemoryRequestResponse
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class MemoryRequestResponseStrategy(
        private val namespaceHandler: NamespaceHandler,
        private val requestMemoryRepository: RequestMemoryRepository
): TypeVerifierStrategy<MemoryRequestResponse> {

    override fun getType(): KClass<MemoryRequestResponse> = MemoryRequestResponse::class

    override fun filterResultList(data: List<MemoryRequestResponse>): List<MemoryRequestResponse> {
        val dataWithAllowedNamespace = data.asSequence()
                .filter { it.namespace != null }
                .filter { namespaceHandler.hasUserAccessToNamespace(it.namespace!!) }
                .toList()

        val requestIdList = dataWithAllowedNamespace.mapNotNull(MemoryRequestResponse::requestId)
        val deniedMemories = requestMemoryRepository.findDeniedMemoriesForAuthorities(requestIdList, getAuthoritiesString())

        if (deniedMemories.isEmpty()) {
            return data
        }

        return data.filter {
            it.requestId !in deniedMemories
        }
    }

    override fun hasUserAccess(data: List<MemoryRequestResponse>): Boolean {
        val hasAccessToAllNamespaces = data.asSequence()
                .mapNotNull(MemoryRequestResponse::namespace)
                .all(namespaceHandler::hasUserAccessToNamespace)

        if (!hasAccessToAllNamespaces) {
            return false
        }

        val requestIdList = data.mapNotNull(MemoryRequestResponse::requestId)
        val deniedMemories = requestMemoryRepository.findDeniedMemoriesForAuthorities(requestIdList, getAuthoritiesString())

        return deniedMemories.isEmpty()
    }

}