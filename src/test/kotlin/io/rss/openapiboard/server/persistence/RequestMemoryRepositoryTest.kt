package io.rss.openapiboard.server.persistence

import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.AppOperation
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.request.HeadersMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.persistence.entities.request.RequestVisibility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.validation.Validator

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class RequestMemoryRepositoryTest {

    @Inject
    lateinit var tested: RequestMemoryRepository

    @Inject
    lateinit var em: EntityManager

    var operationId: Int? = null

    @BeforeEach
    internal fun setUp() {
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()

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
        }
        em.persist(ope)
        em.flush()
        operationId = ope.id
    }

    @Test
    fun persistRequest() {
        val request = RequestMemory().apply {
            body ="""
                {
                    "openAPI": "v3",
                     "basePath": "/test",
                     paths: []
                }
            """.trimIndent()
            title = "My test Request"
            operation = em.getReference(AppOperation::class.java, operationId)
            visibility = RequestVisibility.PUBLIC
            contentType = "application/json"
        }
        request.headers.add(HeadersMemory().apply {
            name = "contentType"
            value = "application/json"
        })


        tested.saveAndFlush(request)
    }

    @Test
    internal fun removeHeaderQuery() {
        tested.clearUpHeaders(1) // just checks query syntax
    }

    @Test
    internal fun deleteRequest() {
        tested.deleteOperationRequest(1, 1L)
    }

    @Test
    fun findByAppNamespace() {
        tested.findByAppNamespace(AppRecord().apply {
            name = "TestApp"
            namespace = "Production"
        })
    }
}