package io.rss.apicenter.server.persistence

import io.rss.apicenter.server.persistence.dao.RequestSampleRepository
import io.rss.apicenter.server.persistence.entities.ApiOperation
import io.rss.apicenter.server.persistence.entities.ApiRecord
import io.rss.apicenter.server.persistence.entities.request.ParameterSample
import io.rss.apicenter.server.persistence.entities.request.ParameterType
import io.rss.apicenter.server.persistence.entities.request.RequestSample
import io.rss.apicenter.server.persistence.entities.request.RequestVisibility
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import javax.annotation.Resource
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class RequestSampleRepositoryTest {

    @Resource
    lateinit var tested: RequestSampleRepository

    @Resource
    lateinit var em: EntityManager

    var operationId: Int? = null
    var operationIdMaster: Int? = null

    companion object {
        val fileContent = RequestSampleRepositoryTest::class.java
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
    }

    @BeforeEach
    internal fun setUp() {
        val api = em.merge(ApiRecord("ricardo", "testing", "2.0").apply {
            basePath = "/base-path"
            modifiedDate = LocalDateTime.now()
            apiUrl = "http://server"
            source = fileContent
        })
        val ope = ApiOperation(api).apply {
            path = "/books"
            this.methodType = MethodType.POST
        }
        em.persist(ope)

        val appMaster = em.merge(ApiRecord(api.name, "master", api.version).apply {
            source = fileContent
        })
        val opeMaster = em.merge(ApiOperation(appMaster).apply {
            path = "/books"
            this.methodType = MethodType.POST
        })
        em.flush()
        operationId = ope.id
        operationIdMaster = opeMaster.id
    }

    @ParameterizedTest
    @CsvSource("'My test Request', 'testValue'")
    fun persistRequest(pTitle: String, someValue: String) {
        val request = RequestSample(em.getReference(ApiOperation::class.java, operationId)).apply {
            body ="""
                {
                    "openAPI": "v3",
                    "description": "/test",
                    "some-key": $someValue
                }
            """.trimIndent()
            title = pTitle
            visibility = RequestVisibility.PUBLIC
            contentType = "application/json"
        }
        request.addParameterSample(ParameterSample().apply {
            name = "contentType"
            value = "application/json"
            parameterType = ParameterType.HEADER
        })
        request.addParameterSample(ParameterSample().apply {
            name = "cache"
            value = "false"
            parameterType = ParameterType.PATH
        })

        tested.saveAndFlush(request)
    }

    @Test
    fun findByAppNamespace() {
        tested.findByApiNamespace("TestApp", "Production")
    }

    @Test
    fun `query result with masters examples`() {
        persistRequest("First request", "Munich")
        persistRequest("Second request", "Tokyo")

        val request = RequestSample(em.getReference(ApiOperation::class.java, operationIdMaster)).apply {
            body ="""
                {
                    "openAPI": "v2",
                    "description": "/fromMaster",
                }
            """.trimIndent()
            title = "ricardo"
            visibility = RequestVisibility.PUBLIC
            contentType = "application/json"
        }

        em.persist(request)
        em.flush()
        em.clear()

        val result = tested.findByApiNamespace("ricardo", "testing")

        assertAll(
                { assertEquals(3, result.size) },
                { assertEquals(1, result[0].parameters.filter { it.parameterType == ParameterType.HEADER }.size)  }
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

    @Test
    fun `check authorities access`() {
        val result = tested.findDeniedSamplesForAuthorities(listOf(1), listOf("TEST_ROLE"))
    }
}