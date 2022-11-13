package io.rss.openapiboard.server.persistence

import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.Resource
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class ApiRecordRepositoryTest {

    @Resource
    lateinit var tested: ApiRecordRepository

    @Resource
    lateinit var em: EntityManager

    @BeforeEach
    internal fun setUp() {
        tested.save(ApiRecord("RicardoApp", "test", "v1"))
        tested.save(ApiRecord("DiffNS", "other", "v1"))
        tested.save(ApiRecord("Yes", "test", "v1"))
    }

    @Test
    internal fun `find namespaces`() {
        val res = tested.findAllNamespace()
        assert(res.size == 2)
        assert("test" in res)
        assert("task" !in res)
    }

    @Test
    internal fun `find all on namespace`() {
        val res = tested.findApiListByNamespace("test")
        assert(res.size == 2)
    }

    @Test
    internal fun `insert and update`() {
        val res = tested.count()
        assert(res == 3L)
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.save(ApiRecord("NewOne", "test", "v3").apply {
            apiUrl = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })  // 4
        tested.save(ApiRecord("Yes", "test", "v3"))   // same
        tested.save(ApiRecord("Yes", "more", "v3"))   // different

        val res2 = tested.count()
        assert(res2 == 5L)
    }

    @Test
    internal fun `get with sources`() {
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()

        tested.save(ApiRecord("NewOne", "test", "v3").apply {
            apiUrl = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })
        tested.flush()
        em.clear()

        val res = tested.getOne(ApiRecordId("NewOne", "test"))
        assertNotNull(res)
        assertEquals(fileContent, res.source)
    }

    @Test
    internal fun `insert roles`() {
        // TODO update
//        val res = tested.save(ApiRecord("Super", "secret", "v2").apply {
//            allowedAuthorities.add("ADMIN")
//            allowedAuthorities.add("MASTER")
//        })
//        assert(res.allowedAuthorities.size == 2)
    }
}