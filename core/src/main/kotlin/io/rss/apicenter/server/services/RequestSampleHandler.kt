package io.rss.apicenter.server.services

import io.rss.apicenter.server.helper.assertGetStringsRequired
import io.rss.apicenter.server.helper.assertRequired
import io.rss.apicenter.server.helper.assertValid
import io.rss.apicenter.server.persistence.MethodType
import io.rss.apicenter.server.persistence.dao.ApiOperationRepository
import io.rss.apicenter.server.persistence.dao.RequestSampleRepository
import io.rss.apicenter.server.persistence.entities.ApiOperation
import io.rss.apicenter.server.persistence.entities.RequestSampleAuthority
import io.rss.apicenter.server.persistence.entities.request.ParameterSample
import io.rss.apicenter.server.persistence.entities.request.ParameterType
import io.rss.apicenter.server.persistence.entities.request.RequestSample
import io.rss.apicenter.server.persistence.entities.request.RequestVisibility
import io.rss.apicenter.server.security.Roles
import io.rss.apicenter.server.services.accesscontrol.AssertRequiredAuthorities
import io.rss.apicenter.server.services.exceptions.BoardApplicationException
import io.rss.apicenter.server.services.to.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.validation.Validator
import javax.ws.rs.core.MediaType
/** Handles CRUD operations related to RequestSample and conversions to related TOs */

@Service
@PreAuthorize("hasAuthority('${Roles.READER}')")
class RequestSampleHandler (
        private val requestRepository: RequestSampleRepository,
        private val operationRepository: ApiOperationRepository,
        private val validator: Validator
) {

    @Transactional
    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @AssertRequiredAuthorities
    fun createRequest(request: RequestSampleInput): RequestSampleResponse {
        require(request.requestId == null) { "Request must not have an id, when creating a new one" }
        return saveRequest(request)
    }

    @Transactional
    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @AssertRequiredAuthorities
    fun saveRequest(request: RequestSampleInput): RequestSampleResponse {
        val requestSample = resolveRequestOperation(request)

        validator.validate(request)
        processParameters(request, requestSample)
        return requestRepository.save(requestSample).let(this::mapSampleToView)
    }

    private fun resolveRequestOperation(request: RequestSampleInput): RequestSample {
        val invalidRequest = {"Request invalid. The follow fields are required: ApiName, namespace, path, http method, title"}

        val apiId = assertRequired(request.apiId, invalidRequest)
        val (path, _) = with(request) {
            assertGetStringsRequired(invalidRequest, path, title)
        }
        val methodType = assertRequired(request.methodType, invalidRequest)

        val requestDb = request.requestId?.let(requestRepository::findByIdOrNull)
        requestDb?.let {
            return updateRequest(it, request) // existing request
        }

        val operation = loadApiOperation(apiId, path, methodType, request)

        return createRequestSample(operation, request)
    }

    private fun updateRequest(existingSample: RequestSample, inputRequest: RequestSampleInput): RequestSample {
        with(existingSample) {
            // fields already validated on #resolveRequestOperation
            title = inputRequest.title!!
            body = inputRequest.body
            namespaceAttached = inputRequest.sameNamespaceOnly
            requiredAuthorities.clear()
            inputRequest.requiredAuthorities?.let {requestAuths ->
                requiredAuthorities.addAll(requestAuths.map { auth -> RequestSampleAuthority(this, auth) })
            }
        }
        return existingSample
    }

    private fun loadApiOperation(apiId: UUID, path: String, methodType: MethodType, request: RequestSampleInput): ApiOperation {
        return operationRepository.findSingleMatch(apiId, path, methodType)
            ?: throw BoardApplicationException("""No operation was found matching the request:  
                |[Api: ${request.apiId}  
                |Path: ${request.path}, method: ${request.methodType}]""".trimMargin())
    }

    private fun createRequestSample(operation: ApiOperation, request: RequestSampleInput) =
        RequestSample(operation).apply {
            visibility = RequestVisibility.PUBLIC       // FUTURE: control where the request is available
            title = request.title!!
            body = request.body
            namespaceAttached = request.sameNamespaceOnly
            contentType = MediaType.APPLICATION_JSON       // FUTURE: receive from request. For now, supports only JSON
            request.requiredAuthorities?.let { authsList ->
                requiredAuthorities.addAll(authsList.map { RequestSampleAuthority(this, it) })
            }
        }

    private fun processParameters(sourceRequest: RequestSampleInput, target: RequestSample) {
        target.parameters.clear()
        sourceRequest.parameters.filter { it.kind != ParameterType.HEADER }.forEach { pr ->
            val paramManaged = ParameterSample(pr.id).apply {
                this.value = pr.value
                this.parameterType = pr.kind
                this.name = pr.name
            }
            target.addParameterSample(paramManaged)
        }
        sourceRequest.requestHeaders.forEach { h ->
            target.addParameterSample(ParameterSample(h.id).apply {
                this.name = h.name
                this.value = h.value
                this.parameterType = ParameterType.HEADER
            })
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun removeRequest(requestId: Long) {
        requestRepository.deleteById(requestId)
    }

    /** Searches based on sample title or operation path, matching start.
     * @see QueryResult
     * */
    @Transactional(readOnly = true)
    @AssertRequiredAuthorities
    fun search(query: String?, offset: Int): QueryResult<RequestSampleResponse> {
        assertValid((query?.length ?: 0) >= MIN_SIZE_SEARCHING) {
            "This query requires at least $MIN_SIZE_SEARCHING characters. Found: ${query?.length}" }

        val data = requestRepository.findRequestsByFilter(query ?: "", PageRequest.of(offset, QUERY_PAGE_SIZE))
                .map(::mapSampleToView)
        return QueryResult(data, (data.size < QUERY_PAGE_SIZE && offset == 0))
    }

    private fun mapSampleToView(source: RequestSample): RequestSampleResponse {
        return RequestSampleResponse(source.id,
                source.operation.apiRecord.id,
                source.namespaceAttached,
                source.operation.path,
                source.operation.methodType).apply {

            this.title = source.title
            this.body = source.body

            source.parameters.filter { it.parameterType != ParameterType.HEADER }
                    .mapTo(this.parameters){ ParameterSampleTO(it.id, it.parameterType, it.name, it.value) }

            source.parameters
                    .filter { it.parameterType == ParameterType.HEADER }
                    .mapTo(this.requestHeaders){ ParameterSampleTO(it.id, it.parameterType, it.name, it.value) }
        }
    }

    private companion object {
        const val QUERY_PAGE_SIZE = 300
        const val MIN_SIZE_SEARCHING = 2
    }

}

