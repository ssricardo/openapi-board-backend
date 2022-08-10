package io.rss.openapiboard.server.services

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.dao.ApiOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import io.rss.openapiboard.server.persistence.entities.request.ParameterMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import javax.ws.rs.core.MediaType

internal class ApiSourceProcessorTest {

    @InjectMocks
    val tested = ApiSourceProcessor()

    @Mock
    lateinit var operationRepository: ApiOperationRepository

    @Mock
    lateinit var requestRepository: RequestMemoryRepository

    private lateinit var sourceTest: String

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.initMocks(this)
        sourceTest = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.init()
    }

    @Test
    fun processSourceOk() {
        tested.processApiRecord(ApiRecord().apply {
            source = sourceTest
        })

        verify(operationRepository, atLeastOnce()).save(any())
    }

    @Test
    fun processBadSource() {
        tested.processApiRecord(ApiRecord().apply {
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
        tested.processApiRecord(ApiRecord())
        verify(operationRepository, times(0)).save(any())
    }

    @Test
    fun processSourceApi2Json() {
        val version2Source = javaClass
                .getResource("/test-data/api2-with-examples.json")
                .readText()
        tested.processApiRecord(ApiRecord().apply {
            source = version2Source
        })
        verify(operationRepository, atLeastOnce()).save(any())
    }

    @Test
    fun enrichNoMemory() {
        val record = createSimpleAppRecord()
        whenever(requestRepository.findByApiNamespace(record.name!!, record.namespace!!)) doReturn listOf()
        tested.enrichApiRecordSource(record)
//        println(record.source)
    }

    @Test
    fun enrichSingleMemoryMatching() {
        val record = createSimpleAppRecord()
        val sampleOp = ApiOperation(10).apply { path = "/pets"; apiRecord = record; methodType = MethodType.POST }
        whenever(requestRepository.findByApiNamespace(record.name!!, record.namespace!!)).thenReturn(arrayListOf(
                createRexMemory(sampleOp, MediaType.APPLICATION_JSON) ))
        val result = tested.enrichApiRecordSource(record)
        assert(sourceTest != result.source)
        assert(result.source?.contains("examples") == true)
        assert(result.source?.contains("Rex") == true)
    }

    @ParameterizedTest
    @CsvSource("/patos,application/json,POST",
            "/pets,application/yml,POST",
            "/pets,application/json,PATCH")
    fun enrichSingleMemoryNonMatchingMatch(paramPath: String, paramContent: String, paramMethod: String) {
        val record = createSimpleAppRecord()
        val sampleOp = ApiOperation(10).apply { path = paramPath; apiRecord = record; methodType = MethodType.valueOf(paramMethod) }
        whenever(requestRepository.findByApiNamespace(record.name!!, record.namespace!!)).thenReturn(arrayListOf(
                createRexMemory(sampleOp, paramContent)
        ))
        val result = tested.enrichApiRecordSource(record)
        assert(result.source?.contains("examples") == false)
        assert(result.source?.contains("Rex") == false)
    }

    @Test
    fun enrichMultipleMatching() {
        val record = createSimpleAppRecord()
        val sampleOp = ApiOperation(10).apply { this.path = "/pets"; this.apiRecord = record }
        whenever(requestRepository.findByApiNamespace(record.name!!, record.namespace!!)).thenReturn(arrayListOf(
            createRexMemory(sampleOp, MediaType.APPLICATION_JSON),
            RequestMemory(7897).apply {
                this.title = "Clients new"
                this.body = """{"id": 10101, "name": "Pluto"}"""
                this.operation = sampleOp
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
        val operation = ApiOperation(10).apply {
            this.path = "/pets/{id}"; this.apiRecord = record; this.methodType = MethodType.GET }
        val memory = createRexMemory(operation, MediaType.APPLICATION_JSON)
        memory.parameters.add(ParameterMemory().apply {
            this.value = "pipoca";
            this.parameterType = ParameterType.PATH;
            this.name = "id"
        })
        memory.parameters.add(ParameterMemory().apply {
            this.value = "Milano"
            this.parameterType = ParameterType.PATH;
            this.name = "city"
        })
        memory.parameters.add(ParameterMemory().apply {
            this.value = "idOnQuery"
            this.parameterType = ParameterType.QUERY;
            this.name = "id"
        })
        Mockito.`when`(requestRepository.findByApiNamespace(record.name!!, record.namespace!!)).thenReturn(arrayListOf(memory))

        val result = tested.enrichApiRecordSource(record)
        assert(result.source?.contains("pipoca") == true)
        assert(result.source?.contains("Milano") == false)
        assert(result.source?.contains("idOnQuery") == false)
    }

    private fun createRexMemory(sampleOp: ApiOperation, paramContent: String): RequestMemory =
        RequestMemory(1).apply {
            this.title = "Clients with account"
            this.body = """{"id": 33, "name": "Rex"}"""
            this.operation = sampleOp
            this.contentType = paramContent
        }

    private fun createSimpleAppRecord(): ApiRecord =
        ApiRecord("ricardoApp", "test").apply {
            source = sourceTest
        }
}