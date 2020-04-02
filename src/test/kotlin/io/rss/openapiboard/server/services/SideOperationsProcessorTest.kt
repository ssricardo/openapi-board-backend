package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.AppOperationType
import io.rss.openapiboard.server.persistence.dao.AppOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.AppOperation
import io.rss.openapiboard.server.persistence.entities.AppRecord
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

internal class SideOperationsProcessorTest {

    @InjectMocks
    val tested = SideOperationsProcessor()

    @Mock
    lateinit var operationRepository: AppOperationRepository

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
        tested.processAppRecord(AppRecord().apply {
            source = sourceTest
        })

        verify(operationRepository, atLeastOnce()).save(any())
    }

    @Test
    fun processBadSource() {
        tested.processAppRecord(AppRecord().apply {
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
        tested.processAppRecord(AppRecord())
        verify(operationRepository, times(0)).save(any())
    }

    @Test
    fun processSourceApi2Json() {
        val version2Source = javaClass
                .getResource("/test-data/api2-with-examples.json")
                .readText()
        tested.processAppRecord(AppRecord().apply {
            source = version2Source
        })
        verify(operationRepository, atLeastOnce()).save(any())
    }

    // ====== AppRecord enrichment =====
    // To test:
    // exemplos de diferentes contentTypes
    // diferentes methods
    // Multiples examples for same operation

    @Test
    fun enrichNoMemory() {
        val record = createSimpleAppRecord()
        Mockito.`when`(requestRepository.findByAppNamespace(record)).thenReturn(listOf())
        tested.enrichAppRecordSource(record)
    }

    @Test
    fun enrichSingleMemoryMatching() {
        val record = createSimpleAppRecord()
        val sampleOp = AppOperation(10).apply { path = "/pets"; appRecord = record; methodType = AppOperationType.POST }
        Mockito.`when`(requestRepository.findByAppNamespace(record)).thenReturn(arrayListOf(
                createRexMemory(sampleOp, MediaType.APPLICATION_JSON) ))
        val result = tested.enrichAppRecordSource(record)
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
        val sampleOp = AppOperation(10).apply { path = paramPath; appRecord = record; methodType = AppOperationType.valueOf(paramMethod) }
        Mockito.`when`(requestRepository.findByAppNamespace(record)).thenReturn(arrayListOf(
                createRexMemory(sampleOp, paramContent)
        ))
        val result = tested.enrichAppRecordSource(record)
        assert(result.source?.contains("examples") == false)
        assert(result.source?.contains("Rex") == false)
    }

    @Test
    fun enrichMultipleMatching() {
        val record = createSimpleAppRecord()
        val sampleOp = AppOperation(10).apply { this.path = "/pets"; this.appRecord = record }
        Mockito.`when`(requestRepository.findByAppNamespace(record)).thenReturn(arrayListOf(
            createRexMemory(sampleOp, MediaType.APPLICATION_JSON),
            RequestMemory(7897).apply {
                this.title = "Clients new"
                this.body = """{"id": 10101, "name": "Pluto"}"""
                this.operation = sampleOp
                this.contentType = MediaType.APPLICATION_JSON
            }
        ))
        val result = tested.enrichAppRecordSource(record)
        println(result.source)
        assert(result.source?.contains("examples") == false)
        assert(result.source?.contains("Rex") == false)
        assert(result.source?.contains("Pluto") == false)
    }

    private fun createRexMemory(sampleOp: AppOperation, paramContent: String): RequestMemory =
        RequestMemory(1).apply {
            this.title = "Clients with account"
            this.body = """{"id": 33, "name": "Rex"}"""
            this.operation = sampleOp
            this.contentType = paramContent
        }

    private fun createSimpleAppRecord(): AppRecord =
        AppRecord("ricardoApp", "test").apply {
            source = sourceTest
        }
}