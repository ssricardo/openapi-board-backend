package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.ApiVersionResponse
import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class ApiVersionResponseStrategy (
        private val repository: ApiRecordRepository
): TypeVerifierStrategy<ApiVersionResponse> {

    override fun getType(): KClass<ApiVersionResponse> = ApiVersionResponse::class

    override fun filterResultList(data: List<ApiVersionResponse>): List<ApiVersionResponse> {
        val idList = data.map { it.apiId }

        val deniedIds = repository.findDeniedApisForAuthorities(idList, getAuthoritiesString())
        if (deniedIds.isEmpty()) {
            return data
        }

        return data.filter {
            it.apiId !in deniedIds
        }
    }

    override fun hasUserAccess(data: List<ApiVersionResponse>) =
            throw IllegalAccessException("Unexpected call in Request flow for ApiVersionResponse")

}