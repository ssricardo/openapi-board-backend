package io.rss.openapiboard.server.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import io.rss.openapiboard.server.helper.assertRequired
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.AppOperationType
import io.rss.openapiboard.server.persistence.dao.AppOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.AppOperation
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.request.*
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.RequestMemoryInputTO
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.MediaType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.Validator

@Service
class SideOperationsProcessor {

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(SideOperationsProcessor::class.java)
    }

    @Inject
    private lateinit var operationRepository: AppOperationRepository

    @Inject
    private lateinit var requestRepository: RequestMemoryRepository

    @Inject
    private lateinit var validator: Validator

    private lateinit var parser:OpenAPIParser
    private lateinit var openWriter:ObjectWriter

    @PostConstruct
    fun init() {
        parser = OpenAPIParser()
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        openWriter = mapper.writer()    // FUTURE: For while, only JSON supported
//        openWriter = mapper.writerWithDefaultPrettyPrinter()
    }

    @Async
    @Transactional()
    fun processAppRecord(app: AppRecord) {
        processAppOperations(app)
    }

    private fun processAppOperations(inputApp: AppRecord) {
        inputApp.source ?: return logNoExecution("Source is null for AppRecord ${inputApp.namespace}/${inputApp.name}")
        val parseResult = parser.readContents(inputApp.source, null, null)

        if (parseResult.messages.isNotEmpty()) {
            LOGGER.warn("Processing OpenAPI for record ${inputApp.namespace}/${inputApp.name} result warnings: ${parseResult.messages}")
        }

        parseResult?.openAPI?.paths ?: return logNoExecution("Either parsed OpenAPI or paths is null")
        parseResult.openAPI.paths.forEach { pStr, pathObj -> storePath(inputApp, pStr, pathObj) }
    }

    private fun storePath(inputApp: AppRecord, pStr: String, pathObj: PathItem) {
        pathObj.get?.let { storePath(inputApp, pStr, AppOperationType.GET) }
        pathObj.post?.let { storePath(inputApp, pStr, AppOperationType.POST) }
        pathObj.put?.let { storePath(inputApp, pStr, AppOperationType.PUT) }
        pathObj.delete?.let { storePath(inputApp, pStr, AppOperationType.DELETE) }
        pathObj.patch?.let { storePath(inputApp, pStr, AppOperationType.PATCH) }
    }

    private fun storePath(inputApp: AppRecord, pStr: String, oppType: AppOperationType) {
        operationRepository.save(AppOperation().apply {
            appRecord = inputApp
            path = pStr
            methodType = oppType
        })
    }

    private fun logNoExecution(message: String) {
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Skipping OpenAPI enriching, due to: $message")
        }
    }

    fun listOperationsByApp(appName: String, namespace: String): List<AppOperation> {
        return operationRepository.findByAppNamespace(appName, namespace)
    }

    @Transactional
    fun saveRequest(request: RequestMemoryInputTO): RequestMemory {
        val requestMemory =  resolveRequestOperation(request)

        validator.validate(request)

        requestMemory.id?.let {
            requestRepository.clearUpHeaders(it)
        }
        request.requestHeaders?.forEach { k, v ->
            requestMemory.headers.add(HeadersMemory().apply {
                this.name = k
                this.value = v
            })
        }
        return requestRepository.save(requestMemory)
    }

    private fun resolveRequestOperation(request: RequestMemoryInputTO): RequestMemory {
        request.requestId?.let {
            return requestRepository.getOne(it) // existing request
        }

        val invalidRequest = {"Request invalid. The follow fields are required: AppName, namespace, path, http method"}
        assertStringRequired(request.appName, invalidRequest)
        assertStringRequired(request.namespace, invalidRequest)
        assertStringRequired(request.path, invalidRequest)
        assertStringRequired(request.title, invalidRequest)
        assertRequired(request.methodType, invalidRequest)

        val operation = operationRepository.findSingleMatch(request.appName!!, request.namespace!!, request.path!!, request.methodType!!)
                ?: throw BoardApplicationException("""No operation was found matching the request: 
                    |[App: ${request.appName}, Namespace: ${request.namespace}, Path: ${request.path}, method: ${request.methodType}]""".trimMargin())

        return createRequestMemory(operation, request)
    }

    private fun createRequestMemory(operation: AppOperation, request: RequestMemoryInputTO): RequestMemory {
        return RequestMemory().apply {
            this.operation = operation
            visibility = RequestVisibility.PUBLIC
            this.title = request.title!!
            this.body = request.body
            contentType = javax.ws.rs.core.MediaType.APPLICATION_JSON       // FUTURE: receive from request. For now, supports only JSON
            request.pathParameters?.forEach { k, v ->
                this.parameters.add(ParameterMemory(ParameterKind.PATH, k).apply { this.value = v })
            }
            request.queryParameters?.forEach { k, v ->
                this.parameters.add(ParameterMemory(ParameterKind.QUERY, k).apply { this.value = v })
            }
        }
    }

    @Transactional
    fun removeRequest(operationId: Int, requestId: Long) {
        requestRepository.deleteOperationRequest(operationId, requestId)
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun enrichAppRecordSource(inputApp: AppRecord): AppRecord {
        inputApp.source ?: return inputApp
        val openApi = parser.readContents(inputApp.source, null, null)?.openAPI
        val paths = openApi?.paths ?: return inputApp

        requestRepository.findByAppNamespace(inputApp)
            .filter { it.operation != null }
            .forEachIndexed { index, reqMemory -> processMatchingPath(paths, reqMemory, index) }

        return inputApp.apply { source = openWriter.writeValueAsString(openApi) }
    }

    private fun processMatchingPath(paths: Paths, rm: RequestMemory, index: Int) =
        paths[rm.operation!!.path]?.let { pi -> processMatchingContent(rm, pi, index) }

    private fun processMatchingContent(rm: RequestMemory, pi: PathItem, index: Int) {
        val methodOperation = getHttpMethodForOperation(rm.operation!!, pi)
        rm.parameters?.forEach { memParam ->
            methodOperation?.parameters?.forEachIndexed { index, specParam ->
                if (specParam.name == memParam.name && specParam.`in` == memParam.kind.toString().toLowerCase()) {
                    specParam.examples = specParam.examples ?: mutableMapOf()
                    specParam.examples["oab-example#$index"] = Example().apply { this.value = memParam.value }
                }
            }
        }

        val content = methodOperation?.requestBody?.content ?: return
        val mediaType = rm.contentType ?: return

        content[mediaType]?.let { media ->
            addMemoryAsExample(media, rm, index)
        }
    }

    private fun getHttpMethodForOperation(operation: AppOperation, pi: PathItem): Operation? {
        return when(operation.methodType) {
            AppOperationType.GET -> pi.get
            AppOperationType.DELETE -> pi.delete
            AppOperationType.POST -> pi.post
            AppOperationType.PUT -> pi.put
            AppOperationType.OPTIONS -> pi.options
            else -> null
        }
    }

    private fun addMemoryAsExample(content: MediaType, rm: RequestMemory, index: Int) {
        content.examples = content.examples ?: mutableMapOf()
        content.examples?.let { ex ->
            ex["oab-example#$index"] = Example().apply {
                this.summary = rm.title
                this.value = rm.body
            }
        }
    }

}