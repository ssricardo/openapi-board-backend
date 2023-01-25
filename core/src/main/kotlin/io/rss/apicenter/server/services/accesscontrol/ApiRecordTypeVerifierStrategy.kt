package io.rss.apicenter.server.services.accesscontrol

import io.rss.apicenter.server.persistence.dao.ApiRecordRepository
import io.rss.apicenter.server.persistence.dao.NamespaceCachedRepository
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class ApiRecordTypeVerifierStrategy (
        private val apiRepository: ApiRecordRepository,
        private val namespaceRepository: NamespaceCachedRepository
): TypeVerifierStrategy<ApiRecord> {

    override fun getType(): KClass<ApiRecord> = ApiRecord::class

    override fun hasUserAccess(data: List<ApiRecord>): Boolean {
        val notNewRecords = data.mapNotNull { it.id }
        if (notNewRecords.isEmpty()) {
            /* new records don't have any restriction yet */
            return true
        }

        val userAuths = getAuthoritiesString()
        val hasAccessToAllNamespaces = data.map { it.namespace }.all { hasAccessToNamespace(it, userAuths) }
        if (!hasAccessToAllNamespaces) {
            return false
        }

        return apiRepository.findDeniedApisForAuthorities(notNewRecords, getAuthoritiesString()).isEmpty()
    }

    override fun filterResultList(data: List<ApiRecord>): List<ApiRecord> {
        val idList = data.mapNotNull { it.id }
        val userAuths = getAuthoritiesString()

        val deniedIds = apiRepository.findDeniedApisForAuthorities(idList, getAuthoritiesString())

        return data.asSequence()
                .filter { it.id !in deniedIds }
                .filter { hasAccessToNamespace(it.namespace, userAuths) }
                .toList()
    }

    private fun hasAccessToNamespace(namespaceId: String, userAuths: List<String>): Boolean {
        val requiredAuths = namespaceRepository.getAuthorities(namespaceId) ?: listOf()
        return requiredAuths.none { it !in userAuths }
    }
}