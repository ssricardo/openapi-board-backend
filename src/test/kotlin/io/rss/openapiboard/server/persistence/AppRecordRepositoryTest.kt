package io.rss.openapiboard.server.persistence

import io.rss.openapiboard.server.persistence.dao.AppRecordRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.inject.Inject
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class AppRecordRepositoryTest {

    @Inject
    lateinit var tested: AppRecordRepository

    @Inject
    lateinit var em: EntityManager

    @BeforeEach
    internal fun setUp() {
        tested.save(AppRecord("RicardoApp", "test"))
        tested.save(AppRecord("DiffNS", "other"))
        tested.save(AppRecord("Yes", "test"))
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
        val res = tested.findAppsByNamespace("test")
        assert(res.size == 2)
    }

    @Test
    internal fun `insert and update`() {
        val res = tested.count()
        assert(res == 3L)
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.save(AppRecord("NewOne", "test").apply {
            version = "3"
            address = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })  // 4
        tested.save(AppRecord("Yes", "test"))   // same
        tested.save(AppRecord("Yes", "more"))   // different

        val res2 = tested.count()
        assert(res2 == 5L)
    }

    @Test
    internal fun `get with sources`() {
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()

        tested.save(AppRecord("NewOne", "test").apply {
            version = "3"
            address = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })
        tested.flush()
        em.clear()

        val res = tested.getOne(AppRecordId("NewOne", "test"))
        assertNotNull(res)
        assertEquals(fileContent, res.source)
    }

    @Test
    internal fun `insert roles`() {
        val res = tested.save(AppRecord("Super", "secret").apply {
            allowedRoles.add("ADMIN")
            allowedRoles.add("MASTER")
        })
        assert(res.allowedRoles.size == 2)
    }
}