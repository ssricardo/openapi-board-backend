package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class ApiRecordTypeVerifierStrategy (
        val repository: ApiRecordRepository
): TypeVerifierStrategy<ApiRecord> {

    override fun getType(): KClass<ApiRecord> = ApiRecord::class

    override fun hasUserAccess(data: List<ApiRecord>): Boolean {
        val notNewRecords = data.mapNotNull { it.id }
        if (notNewRecords.isEmpty()) {
            /* new records don't have any restriction yet */
            return true
        }


        return repository.findDeniedApisForAuthorities(notNewRecords, getAuthoritiesString()).isEmpty()
    }

    override fun filterResultList(data: List<ApiRecord>): List<ApiRecord> {
        val idList = data.mapNotNull { it.id }

        val deniedIds = repository.findDeniedApisForAuthorities(idList, getAuthoritiesString())
        if (deniedIds.isEmpty()) {
            return data
        }

        return data.filter {
            it.id !in deniedIds
        }
    }
}