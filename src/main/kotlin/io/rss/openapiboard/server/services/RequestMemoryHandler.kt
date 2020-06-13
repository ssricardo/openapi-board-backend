package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.config.security.Roles
import io.rss.openapiboard.server.helper.assertRequired
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.helper.assertValid
import io.rss.openapiboard.server.persistence.dao.AppOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.AppOperation
import io.rss.openapiboard.server.persistence.entities.request.ParameterKind
import io.rss.openapiboard.server.persistence.entities.request.ParameterMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestVisibility
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.ParameterMemoryTO
import io.rss.openapiboard.server.services.to.QueryResult
import io.rss.openapiboard.server.services.to.RequestMemoryViewTO
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.Validator

/** Handles CRUD operations related to RequestMemory and conversions to related TOs */

@Service
@PreAuthorize("hasAuthority('${Roles.READER}')")
class RequestMemoryHandler {

    @Inject
    private lateinit var requestRepository: RequestMemoryRepository

    @Inject
    private lateinit var operationRepository: AppOperationRepository

    @Inject
    private lateinit var validator: Validator

    companion object {
        const val QUERY_PAGE_SIZE = 300
        const val MIN_SIZE_SEARCHING = 2
    }

    @Transactional()
    @PreAuthorize("hasAuthority('${Roles.MANAGER}')")
    fun saveRequest(request: RequestMemoryViewTO): RequestMemory {
        val requestMemory =  resolveRequestOperation(request)

        validator.validate(request)
        processParameters(request, requestMemory)
        return requestRepository.save(requestMemory)
    }

    private fun resolveRequestOperation(request: RequestMemoryViewTO): RequestMemory {
        val invalidRequest = {"Request invalid. The follow fields are required: AppName, namespace, path, http method, title"}
        assertStringRequired(request.appName, invalidRequest)
        assertStringRequired(request.namespace, invalidRequest)
        assertStringRequired(request.path, invalidRequest)
        assertStringRequired(request.title, invalidRequest)
        assertRequired(request.methodType, invalidRequest)

        request.requestId?.let {
            return updateRequest(requestRepository.getOne(it), request) // existing request
        }

        val operation = operationRepository.findSingleMatch(request.appName!!, request.namespace!!, request.path!!, request.methodType!!)
                ?: throw BoardApplicationException("""No operation was found matching the request: 
                    |[App: ${request.appName}, Namespace: ${request.namespace}, Path: ${request.path}, method: ${request.methodType}]""".trimMargin())

        return createRequestMemory(operation, request)
    }

    private fun updateRequest(existingMemory: RequestMemory, inputRequest: RequestMemoryViewTO): RequestMemory {
        with(existingMemory) {
            // fields already validated on #resolveRequestOperation
            title = inputRequest.title!!
            body = inputRequest.body
        }
        return existingMemory
    }

    private fun createRequestMemory(operation: AppOperation, request: RequestMemoryViewTO): RequestMemory {
        return RequestMemory().apply {
            this.operation = operation
            visibility = RequestVisibility.PUBLIC
            this.title = request.title!!
            this.body = request.body
            contentType = javax.ws.rs.core.MediaType.APPLICATION_JSON       // FUTURE: receive from request. For now, supports only JSON
        }
    }

    private fun processParameters(sourceRequest: RequestMemoryViewTO, target: RequestMemory) {
        target.parameters.clear()
        sourceRequest.parameters?.filter { it.kind != ParameterKind.HEADER }?.forEach { pr ->
            val paramManaged = ParameterMemory(pr.id).apply {
                this.value = pr.value
                this.kind = pr.kind
                this.name = pr.name
            }
            target.addParameterMemory(paramManaged)
        }
        sourceRequest.requestHeaders?.forEach { h ->
            target.addParameterMemory(ParameterMemory(h.id).apply {
                this.name = h.name
                this.value = h.value
                this.kind = ParameterKind.HEADER;
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true) // for lazy loading. provider specific due to readOnly
    fun search(query: String?, offset: Int): QueryResult<List<RequestMemoryViewTO>> {
        assertValid((query?.length ?: 0) >= MIN_SIZE_SEARCHING) {
            "This query requires at least $MIN_SIZE_SEARCHING characteres. Found: ${query?.length}" }

        val data = requestRepository.findRequestsByFilter(query ?: "", PageRequest.of(offset, QUERY_PAGE_SIZE))
                .map(::convertMemoryToView)
        return QueryResult(data, (data.size < QUERY_PAGE_SIZE && offset == 0))
    }

    private fun convertMemoryToView(source: RequestMemory): RequestMemoryViewTO {
        return RequestMemoryViewTO(source.id, source.operation?.appRecord?.namespace, source.operation?.appRecord?.name,
                source.operation?.path, source.operation?.methodType).apply {
            this.title = source.title
            this.body = source.body

            source.parameters.filter { it.kind != ParameterKind.HEADER }
                    .mapTo(this.parameters){ParameterMemoryTO(it.id, it.kind, it.name, it.value)}

            source.parameters
                    .filter { it.kind == ParameterKind.HEADER }
                    .mapTo(this.requestHeaders){ParameterMemoryTO(it.id, it.kind, it.name, it.value)}
        }
    }
}