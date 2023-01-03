package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.helper.assertGetStringsRequired
import io.rss.openapiboard.server.helper.assertRequired
import io.rss.openapiboard.server.helper.assertValid
import io.rss.openapiboard.server.persistence.dao.ApiOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.request.ParameterMemory
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestVisibility
import io.rss.openapiboard.server.security.Roles
import io.rss.openapiboard.server.services.accesscontrol.AssertRequiredAuthorities
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.ParameterMemoryTO
import io.rss.openapiboard.server.services.to.QueryResult
import io.rss.openapiboard.server.services.to.MemoryRequestResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.validation.Validator
import javax.ws.rs.core.MediaType

/** Handles CRUD operations related to RequestMemory and conversions to related TOs */

@Service
@PreAuthorize("hasAuthority('${Roles.READER}')")
class RequestMemoryHandler (
    private val requestRepository: RequestMemoryRepository,
    private val operationRepository: ApiOperationRepository,
    private val namespaceHandler: NamespaceHandler,
    private val validator: Validator
) {

    @Transactional
    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun createRequest(request: MemoryRequestResponse): MemoryRequestResponse {
        require(request.requestId == null) { "Request must not have an id, when creating a new one" }
        return saveRequest(request)
    }

    @Transactional()
    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    @AssertRequiredAuthorities
    fun saveRequest(request: MemoryRequestResponse): MemoryRequestResponse {
//        request.namespace?.let(namespaceHandler::assertUserHasAccess)     FIXME control access

        val requestMemory =  resolveRequestOperation(request)

        validator.validate(request)
        processParameters(request, requestMemory)
        return requestRepository.save(requestMemory).let(this::mapMemoryToView)
    }

    private fun resolveRequestOperation(request: MemoryRequestResponse): RequestMemory {
        val invalidRequest = {"Request invalid. The follow fields are required: ApiName, namespace, path, http method, title"}

        val (apiName, ns, path, _) = with(request) {
             assertGetStringsRequired(invalidRequest, apiName, namespace, path, title)
        }
        val methodType = assertRequired(request.methodType, invalidRequest)

        val requestDb = request.requestId?.let { requestRepository.findByIdOrNull(it) }
        requestDb?.let {
            return updateRequest(it, request) // existing request
        }

        val operation = operationRepository.findSingleMatch(apiName, ns, path, methodType)
                ?: throw BoardApplicationException(
                    """No operation was found matching the request: 
                    |[Api: ${request.apiName}, Namespace: ${request.namespace}, 
                    |Path: ${request.path}, method: ${request.methodType}]""".trimMargin())

        return createRequestMemory(operation, request)
    }

    private fun updateRequest(existingMemory: RequestMemory, inputRequest: MemoryRequestResponse): RequestMemory {
        with(existingMemory) {
            // fields already validated on #resolveRequestOperation
            title = inputRequest.title!!
            body = inputRequest.body
        }
        return existingMemory
    }

    private fun createRequestMemory(operation: ApiOperation, request: MemoryRequestResponse): RequestMemory {
        return RequestMemory(operation).apply {
            visibility = RequestVisibility.PUBLIC       // FUTURE: control where the request is available
            this.title = request.title!!
            this.body = request.body
            contentType = MediaType.APPLICATION_JSON       // FUTURE: receive from request. For now, supports only JSON
        }
    }

    private fun processParameters(sourceRequest: MemoryRequestResponse, target: RequestMemory) {
        target.parameters.clear()
        sourceRequest.parameters.filter { it.kind != ParameterType.HEADER }.forEach { pr ->
            val paramManaged = ParameterMemory(pr.id).apply {
                this.value = pr.value
                this.parameterType = pr.kind
                this.name = pr.name
            }
            target.addParameterMemory(paramManaged)
        }
        sourceRequest.requestHeaders.forEach { h ->
            target.addParameterMemory(ParameterMemory(h.id).apply {
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

    /** Searches based on memory title or operation path, matching start.
     * @see QueryResult
     * */
    @Transactional(readOnly = true)
    fun search(query: String?, offset: Int): QueryResult<MemoryRequestResponse> {
        assertValid((query?.length ?: 0) >= MIN_SIZE_SEARCHING) {
            "This query requires at least $MIN_SIZE_SEARCHING characters. Found: ${query?.length}" }

        val data = requestRepository.findRequestsByFilter(query ?: "", PageRequest.of(offset, QUERY_PAGE_SIZE))
                .asSequence()
                .filter (this::filterNamespaceIfNeeded)
                .map(::mapMemoryToView)
                .toList()
        return QueryResult(data, (data.size < QUERY_PAGE_SIZE && offset == 0))
    }

    private fun filterNamespaceIfNeeded(requestMemory: RequestMemory): Boolean {
        if (!requestMemory.namespaceAttached) {
            return true
        }

        return namespaceHandler.hasUserAccessToNamespace(requestMemory.operation.apiRecord.namespace)
    }

    private fun mapMemoryToView(source: RequestMemory): MemoryRequestResponse {
        return MemoryRequestResponse(source.id,
                source.operation.apiRecord.namespace,
                source.operation.apiRecord.name,
                source.operation.path,
                source.operation.methodType).apply {
            this.title = source.title
            this.body = source.body

            source.parameters.filter { it.parameterType != ParameterType.HEADER }
                    .mapTo(this.parameters){ParameterMemoryTO(it.id, it.parameterType, it.name, it.value)}

            source.parameters
                    .filter { it.parameterType == ParameterType.HEADER }
                    .mapTo(this.requestHeaders){ParameterMemoryTO(it.id, it.parameterType, it.name, it.value)}
        }
    }

    private companion object {
        const val QUERY_PAGE_SIZE = 300
        const val MIN_SIZE_SEARCHING = 2
    }

}