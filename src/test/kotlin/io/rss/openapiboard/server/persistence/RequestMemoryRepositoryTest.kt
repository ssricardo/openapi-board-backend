package io.rss.openapiboard.server.persistence

import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.AppOperation
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.request.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import javax.inject.Inject
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class RequestMemoryRepositoryTest {

    @Inject
    lateinit var tested: RequestMemoryRepository

    @Inject
    lateinit var em: EntityManager

    var operationId: Int? = null
    var operationIdMaster: Int? = null

    companion object {
        val fileContent = RequestMemoryRepositoryTest::class.java
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
    }

    @BeforeEach
    internal fun setUp() {
        val app = em.merge(AppRecord("ricardo", "testing").apply {
            path = "/base-path"
            version = "2.0"
            modifiedDate = LocalDateTime.now()
            address = "http://server"
            source = fileContent
        })
        val ope = AppOperation().apply {
            appRecord = app
            path = "/books"
            this.methodType = AppOperationType.POST
        }
        em.persist(ope)

        val appMaster = em.merge(app.copy(namespace = "master"))
        val opeMaster = em.merge(ope.copy(id = null).apply { appRecord = appMaster })
        em.flush()
        operationId = ope.id
        operationIdMaster = opeMaster.id
    }

    @ParameterizedTest
    @CsvSource("'My test Request', 'testValue'")
    fun persistRequest(pTitle: String, someValue: String) {
        val request = RequestMemory().apply {
            body ="""
                {
                    "openAPI": "v3",
                    "description": "/test",
                    "some-key": $someValue
                }
            """.trimIndent()
            title = pTitle
            operation = em.getReference(AppOperation::class.java, operationId)
            visibility = RequestVisibility.PUBLIC
            contentType = "application/json"
        }
        request.addParameterMemory(ParameterMemory().apply {
            name = "contentType"
            value = "application/json"
            kind = ParameterKind.HEADER
        })
        request.addParameterMemory(ParameterMemory().apply {
            name = "cache"
            value = "false"
            kind = ParameterKind.PATH
        })

        tested.saveAndFlush(request)
    }

    @Test
    fun findByAppNamespace() {
        tested.findByAppNamespace("TestApp", "Production")
    }

    @Test
    fun `query result with masters examples`() {
        persistRequest("First request", "Munich")
        persistRequest("Second request", "Tokyo")

        val request = RequestMemory().apply {
            body ="""
                {
                    "openAPI": "v2",
                    "description": "/fromMaster",
                }
            """.trimIndent()
            title = "ricardo"
            operation = em.getReference(AppOperation::class.java, operationIdMaster)
            visibility = RequestVisibility.PUBLIC
            contentType = "application/json"
        }

        em.persist(request)
        em.flush()
        em.clear()

        val result = tested.findByAppNamespace("ricardo", "testing")

        assertAll(
                { assertEquals(3, result.size) },
                { assertEquals(1, result[0].parameters.filter { it.kind == ParameterKind.HEADER }.size)  }
        )
    }

    @Test
    internal fun testSearching() {
        persistRequest("Germany", "Munich")
        persistRequest("Georgia", "Tokyo")
        persistRequest("Japan", "Tokyo")
        em.flush()

        val result = tested.findRequestsByFilter("ge", PageRequest.of(0, 100))
        Assertions.assertEquals(2, result.size)
    }
}