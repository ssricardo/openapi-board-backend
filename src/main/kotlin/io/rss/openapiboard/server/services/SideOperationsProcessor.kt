package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.AppOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.AppOperation
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.request.HeadersMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestVisibility
import io.swagger.v3.parser.OpenAPIV3Parser
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.Validator

@Service
class SideOperationsProcessor {

    private val parser = OpenAPIV3Parser()

    @Inject
    private lateinit var operationRepository: AppOperationRepository

    @Inject
    private lateinit var requestRepository: RequestMemoryRepository

    @Inject
    private lateinit var validator: Validator

    @Async
    @Transactional()
    fun processAppRecord(app: AppRecord) {
        processAppOperations(app)
    }

    private fun processAppOperations(inputApp: AppRecord) {
        val parseResult = parser.readContents(inputApp.source)
        if (parseResult.messages.isNotEmpty()) {
            // TODO log and return
            println("Deu ruim... ${parseResult.messages}")
//            return
        }

        parseResult.openAPI.paths.forEach { pStr, _ ->
            operationRepository.saveAndFlush(AppOperation().apply {
                appRecord = inputApp
                path = pStr
            })
        }
    }

    fun listOperationsByApp(appName: String, namespace: String): List<AppOperation> {
        return operationRepository.findByAppNamespace(appName, namespace)
    }

    @Transactional
    fun saveRequest(operationId: Int, request: RequestMemory, requestHeaders: Map<String, String>?): RequestMemory {
        request.visibility = RequestVisibility.PUBLIC
        validator.validate(request)

        request.operation = operationRepository.getOne(operationId)
        request.id?.let {
            requestRepository.clearUpHeaders(it)
        }
        requestHeaders?.forEach { k, v ->
            request.headers.add(HeadersMemory().apply {
                this.name = k
                this.value = v
            })
        }
        return requestRepository.save(request)
    }

    @Transactional
    fun removeRequest(operationId: Int, requestId: Long) {
        requestRepository.deleteOperationRequest(operationId, requestId)
    }
}