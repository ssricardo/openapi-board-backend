package io.rss.openapiboard.server.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.dao.ApiOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.MediaType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.Resource
import javax.transaction.Transactional

/**
 * Handles operations with actual source (API spec) of ApiRecord.
 * Performs validations and especially matching/mixing the original source with related RequestMemory.
 *
 * @see ApiRecord
 * @see RequestMemory
 *
 * @author ricardo saturnino
 */
@Service
class ApiSourceProcessor {

    @Resource
    private lateinit var operationRepository: ApiOperationRepository

    @Resource
    private lateinit var requestRepository: RequestMemoryRepository

    private lateinit var parser:OpenAPIParser

    private lateinit var openWriter:ObjectWriter
    @PostConstruct
    fun init() {
        parser = OpenAPIParser()
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        openWriter = mapper.writer()    // for FUTURE: For while, only JSON supported
//        openWriter = mapper.writerWithDefaultPrettyPrinter()
    }

//    @Async
    @Transactional()
    fun processApiRecord(api: ApiRecord) {
        processApiOperations(api)
    }

    private fun processApiOperations(inputApi: ApiRecord) {
        inputApi.source ?: return logNoExecution("Source is null for ApiRecord ${inputApi.namespace}/${inputApi.name}")
        val parseResult = parser.readContents(inputApi.source, null, null)

        if (parseResult.messages.isNotEmpty()) {
            LOGGER.warn("Processing OpenAPI for record ${inputApi.namespace}/${inputApi.name} result warnings: ${parseResult.messages}")
        }

        parseResult?.openAPI?.paths ?: return logNoExecution("Either parsed OpenAPI or paths is null")
        parseResult.openAPI.paths.forEach { pStr, pathObj -> storePath(inputApi, pStr, pathObj) }
    }

    private fun storePath(inputApi: ApiRecord, pStr: String, pathObj: PathItem) {
        pathObj.get?.let { storePath(inputApi, pStr, MethodType.GET) }
        pathObj.post?.let { storePath(inputApi, pStr, MethodType.POST) }
        pathObj.put?.let { storePath(inputApi, pStr, MethodType.PUT) }
        pathObj.delete?.let { storePath(inputApi, pStr, MethodType.DELETE) }
        pathObj.patch?.let { storePath(inputApi, pStr, MethodType.PATCH) }
    }

    private fun storePath(inputApi: ApiRecord, pathStr: String, oppType: MethodType) {
        val apiName = checkNotNull(inputApi.name)
        val namespace = checkNotNull(inputApi.namespace)

        val operation = operationRepository.findSingleMatch(apiName, namespace, pathStr, oppType)
                ?: ApiOperation().apply {
                    apiRecord = inputApi
                    path = pathStr
                    methodType = oppType
                }

        LOGGER.info("Saving operation [${operation.id}] for app [$apiName, $namespace] , path $pathStr [$oppType]")
        operationRepository.save(operation)
    }

    private fun logNoExecution(message: String) {
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Skipping OpenAPI enriching, due to: $message")
        }
    }

    fun listOperationsByApi(apiName: String, namespace: String): List<ApiOperation> {
        return operationRepository.findByApiNamespace(apiName, namespace)
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun enrichApiRecordSource(inputApi: ApiRecord): ApiRecord {
        inputApi.source ?: return inputApi
        val openApi = parser.readContents(inputApi.source, null, null)?.openAPI
        val paths = openApi?.paths ?: return inputApi

        requestRepository.findByApiNamespace(
                inputApi.name ?: throw IllegalStateException(),
                inputApi.namespace ?: throw IllegalStateException())
            .filter { it.operation != null }
            .forEachIndexed { index, reqMemory -> processMatchingPath(paths, reqMemory, index) }

        return inputApi.apply { source = openWriter.writeValueAsString(openApi) }
    }

    private fun processMatchingPath(paths: Paths, rm: RequestMemory, index: Int) =
        paths[rm.operation!!.path]?.let { pi -> processMatchingContent(rm, pi, index) }

    private fun processMatchingContent(rm: RequestMemory, pi: PathItem, index: Int) {
        val methodOperation = getHttpMethodForOperation(rm.operation!!, pi)
        rm.parameters.forEach { memParam ->
            methodOperation?.parameters?.forEachIndexed { index, specParam ->
                if (specParam.name == memParam.name && specParam.`in` == memParam.parameterType.toString().lowercase()) {
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

    private fun getHttpMethodForOperation(operation: ApiOperation, pi: PathItem): Operation? {
        return when(operation.methodType) {
            MethodType.GET -> pi.get
            MethodType.DELETE -> pi.delete
            MethodType.POST -> pi.post
            MethodType.PUT -> pi.put
            MethodType.OPTIONS -> pi.options
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

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ApiSourceProcessor::class.java)
    }
}
