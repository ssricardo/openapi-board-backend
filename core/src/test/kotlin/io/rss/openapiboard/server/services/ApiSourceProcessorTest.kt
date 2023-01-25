package io.rss.openapiboard.server.services

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.dao.ApiOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestSampleRepository
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import io.rss.openapiboard.server.persistence.entities.request.ParameterSample
import io.rss.openapiboard.server.persistence.entities.request.RequestSample
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*
import javax.ws.rs.core.MediaType

internal class ApiSourceProcessorTest {

    @Mock
    lateinit var operationRepository: ApiOperationRepository

    @Mock
    lateinit var requestRepository: RequestSampleRepository

    @InjectMocks
    lateinit var tested: ApiSourceProcessor

    private lateinit var sourceTest: String

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.openMocks(this)
        sourceTest = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.init()
    }

    @Test
    fun processSourceOk() {
        tested.processApiRecordAsync(ApiRecord("name", "ns", "v1", UUID.randomUUID()).apply {
            source = sourceTest
        })

        verify(operationRepository, atLeastOnce()).save(any())
    }

    @Test
    fun processBadSource() {
        tested.processApiRecordAsync(ApiRecord("name", "ns", "v1").apply {
            source = """
                openapi: "3.0.0"
                info:
                  version: 1.0.0
                  title: Swagger Petstore
                  description: A sample API that uses a petstore as an example to demonstrate features in the OpenAPI 3.0 specification
                
            some unexpected data: not well formatted
            """.trimIndent()
        })
        verify(operationRepository, times(0)).save(any())
    }

    @Test
    fun processEmptySource() {
        tested.processApiRecordAsync(ApiRecord("name", "ns", "v2"))
        verify(operationRepository, times(0)).save(any())
    }

    @Test
    fun processSourceApi2Json() {
        val version2Source = javaClass
                .getResource("/test-data/api2-with-examples.json")
                .readText()
        tested.processApiRecordAsync(ApiRecord("name", "ns", "v2", UUID.randomUUID()).apply {
            source = version2Source
        })
        verify(operationRepository, atLeastOnce()).save(any())
    }

    @Test
    fun enrichNoSample() {
        val record = createSimpleAppRecord()
        whenever(requestRepository.findByApiNamespace(record.name, record.namespace)) doReturn listOf()
        tested.enrichApiRecordSource(record)
    }

    @Test
    fun enrichSingleSampleMatching() {
        val record = createSimpleAppRecord()
        val sampleOp = ApiOperation(record, 10).apply { path = "/pets"; methodType = MethodType.POST }
        whenever(requestRepository.findByApiNamespace(record.name, record.namespace)).thenReturn(arrayListOf(
                createReqSample(sampleOp, MediaType.APPLICATION_JSON) ))
        val result = tested.enrichApiRecordSource(record)
        assert(sourceTest != result.source)
        assert(result.source?.contains("examples") == true)
        assert(result.source?.contains("Rex") == true)
    }

    @ParameterizedTest
    @CsvSource("/patos,application/json,POST",
            "/pets,application/yml,POST",
            "/pets,application/json,PATCH")
    fun enrichSingleSampleNonMatchingMatch(paramPath: String, paramContent: String, paramMethod: String) {
        val record = createSimpleAppRecord()
        val sampleOp = ApiOperation(record, 10).apply { path = paramPath; methodType = MethodType.valueOf(paramMethod) }
        whenever(requestRepository.findByApiNamespace(record.name, record.namespace)).thenReturn(arrayListOf(
                createReqSample(sampleOp, paramContent)
        ))
        val result = tested.enrichApiRecordSource(record)
        assert(result.source?.contains("examples") == false)
        assert(result.source?.contains("Rex") == false)
    }

    @Test
    fun enrichMultipleMatching() {
        val record = createSimpleAppRecord()
        val sampleOp = ApiOperation(record, 10).apply { this.path = "/pets" }
        whenever(requestRepository.findByApiNamespace(record.name, record.namespace)).thenReturn(arrayListOf(
            createReqSample(sampleOp, MediaType.APPLICATION_JSON),
            RequestSample(sampleOp, 7897).apply {
                this.title = "Clients new"
                this.body = """{"id": 10101, "name": "Pluto"}"""
                this.contentType = MediaType.APPLICATION_JSON
            }
        ))
        val result = tested.enrichApiRecordSource(record)
        assert(result.source?.contains("examples") == false)
        assert(result.source?.contains("Rex") == false)
        assert(result.source?.contains("Pluto") == false)
    }

    @Test
    fun examplesForParameters() {
        val record = createSimpleAppRecord()
        val operation = ApiOperation(record, 10).apply {
            this.path = "/pets/{id}"; this.methodType = MethodType.GET }
        val sample = createReqSample(operation, MediaType.APPLICATION_JSON)
        sample.parameters.add(ParameterSample().apply {
            this.value = "pipoca"
            this.parameterType = ParameterType.PATH
            this.name = "id"
        })
        sample.parameters.add(ParameterSample().apply {
            this.value = "Milano"
            this.parameterType = ParameterType.PATH
            this.name = "city"
        })
        sample.parameters.add(ParameterSample().apply {
            this.value = "idOnQuery"
            this.parameterType = ParameterType.QUERY
            this.name = "id"
        })
        whenever(requestRepository.findByApiNamespace(record.name, record.namespace)).thenReturn(arrayListOf(sample))

        val result = tested.enrichApiRecordSource(record)
        assert(result.source?.contains("pipoca") == true)
        assert(result.source?.contains("Milano") == false)
        assert(result.source?.contains("idOnQuery") == false)
    }

    private fun createReqSample(sampleOp: ApiOperation, paramContent: String): RequestSample =
        RequestSample(sampleOp, 1).apply {
            this.title = "Clients with account"
            this.body = """{"id": 33, "name": "Rex"}"""
            this.contentType = paramContent
        }

    private fun createSimpleAppRecord(): ApiRecord =
        ApiRecord("ricardoApp", "test", "v1").apply {
            source = sourceTest
        }
}