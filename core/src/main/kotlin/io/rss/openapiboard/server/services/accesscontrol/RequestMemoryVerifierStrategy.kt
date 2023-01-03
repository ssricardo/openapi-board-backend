package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RequestMemoryVerifierStrategy: TypeVerifierStrategy<RequestMemory> {

    override fun getType(): KClass<RequestMemory> = RequestMemory::class

    override fun filterResultList(data: List<RequestMemory>): List<RequestMemory> {
        TODO("Not yet implemented")
    }

    override fun hasUserAccess(data: List<RequestMemory>): Boolean {
        TODO("Not yet implemented")
    }

}