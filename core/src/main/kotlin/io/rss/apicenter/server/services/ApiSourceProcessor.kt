package io.rss.apicenter.server.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import io.rss.apicenter.server.persistence.MethodType
import io.rss.apicenter.server.persistence.dao.ApiOperationRepository
import io.rss.apicenter.server.persistence.dao.RequestSampleRepository
import io.rss.apicenter.server.persistence.entities.ApiOperation
import io.rss.apicenter.server.persistence.entities.ApiRecord
import io.rss.apicenter.server.persistence.entities.request.RequestSample
import io.rss.apicenter.server.services.exceptions.BoardApplicationException
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.parser.core.models.SwaggerParseResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.transaction.Transactional

/**
 * Handles operations with actual source (API spec) of ApiRecord.
 * Performs validations and especially matching/mixing the original source with related RequestSample.
 *
 * @see ApiRecord
 * @see RequestSample
 *
 * @author ricardo saturnino
 */
@Service
class ApiSourceProcessor(
        private val operationRepository: ApiOperationRepository,
        private val requestRepository: RequestSampleRepository
) {

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

    @Async("threadPoolTaskExecutor")
    @Transactional()
    fun processApiRecordAsync(api: ApiRecord) =
        processApiOperations(api)

    private fun processApiOperations(inputApi: ApiRecord) {
        inputApi.source ?: return logNoExecution("Source is null for ApiRecord ${inputApi.namespace}/${inputApi.name}")
        val parseResult = parseApiResult(inputApi)

        if (parseResult.messages.isNotEmpty()) {
            LOGGER.warn("Processing OpenAPI for record ${inputApi.namespace}/${inputApi.name} result warnings: ${parseResult.messages}")
        }

        parseResult.openAPI?.paths ?: return logNoExecution("Either parsed OpenAPI or paths is null")
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
        val apiId = requireNotNull(inputApi.id) { "Api ID must be not null before storing an Operation" }
        val apiName = checkNotNull(inputApi.name)
        val namespace = checkNotNull(inputApi.namespace)

        val operation = operationRepository.findSingleMatch(apiId, pathStr, oppType)
                ?: ApiOperation(inputApi).apply {
                    path = pathStr
                    methodType = oppType
                }

        LOGGER.info("Saving operation [${operation.id ?: "NEW"}] for app [$apiName, $namespace] , path $pathStr [$oppType]")
        operationRepository.save(operation)
    }

    private fun logNoExecution(message: String) {
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Skipping OpenAPI enriching, due to: $message")
        }
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    fun enrichApiRecordSource(inputApi: ApiRecord): ApiRecord {
        inputApi.source ?: return inputApi
        val openApi = parseApiResult(inputApi).openAPI
        val paths = openApi?.paths ?: return inputApi

        requestRepository.findByApiNamespace(inputApi.name, inputApi.namespace)
            .filter { it.operation.path != null }
            .forEachIndexed { index, reqSample -> processMatchingPath(paths, reqSample, index) }

        return inputApi.apply { source = openWriter.writeValueAsString(openApi) }
    }

    private fun parseApiResult(inputApi: ApiRecord): SwaggerParseResult =
            try {
                parser.readContents(inputApi.source, null, null)
            } catch (e: JsonParseException) {
                throw BoardApplicationException("The API is invalid. It could not be parsed", e)
            }

    private fun processMatchingPath(paths: Paths, rm: RequestSample, index: Int) =
        paths[rm.operation.path]?.let { pi -> processMatchingContent(rm, pi, index) }

    private fun processMatchingContent(rSample: RequestSample, pi: PathItem, index: Int) {
        val methodOperation = getHttpMethodForOperation(rSample.operation, pi)
        rSample.parameters.forEach { requestParam ->
            methodOperation?.parameters?.forEachIndexed { index, specParam ->
                if (specParam.name == requestParam.name && specParam.`in` == requestParam.parameterType.toString().lowercase()) {
                    specParam.examples = specParam.examples ?: mutableMapOf()
                    specParam.examples["oab-example#$index"] = Example().apply { this.value = requestParam.value }
                }
            }
        }

        val content = methodOperation?.requestBody?.content ?: return
        val mediaType = rSample.contentType ?: return

        content[mediaType]?.let { media ->
            addSampleAsOpenApiExample(media, rSample, index)
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

    private fun addSampleAsOpenApiExample(content: MediaType, rm: RequestSample, index: Int) {
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
