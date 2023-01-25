package io.rss.apicenter.server.persistence

import io.rss.apicenter.server.persistence.dao.ApiSnapshotRepository
import io.rss.apicenter.server.persistence.entities.ApiSnapshot
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.Resource
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest()
@Tag("db")
class ApiSnapshotRepositoryTest {

    @Resource
    lateinit var tested: ApiSnapshotRepository

    @Resource
    lateinit var em: EntityManager

    @BeforeEach
    internal fun setUp() {
        tested.save(ApiSnapshot("RicardoApp", "test", "1.0"))
        tested.save(ApiSnapshot("RicardoApp", "test", "2.0"))
        tested.save(ApiSnapshot("RicardoApp", "test", "3.0"))
        tested.save(ApiSnapshot("DiffNS", "other", "5.0"))
        tested.save(ApiSnapshot("Yes", "test", "2.0"))
    }

    @Test
    internal fun `find namespaces`() {
        val res = tested.findApiVersionList("RicardoApp", "test")
        assert(res.size == 3)
        assert("2.0" in res)
        assert("5.0" !in res)
    }

    @Test
    fun `find previous version`() {
        val res = tested.findTopPreviousVersion("RicardoApp", "test", "3.0",
            PageRequest.of(0, 1)).first()
        assertNotNull(res)
        assertFalse(res.version == "3.0")
    }

    @Test
    internal fun `insert and update`() {
        val res = tested.count()
        assert(res == 5L)
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.save(ApiSnapshot("NewOne", "test", "3").apply {
            apiUrl = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })  // 4
        tested.save(ApiSnapshot("Yes", "more", "3"))   // same
        tested.save(ApiSnapshot("NewOne", "test", "3"))

        val res2 = tested.count()
        assert(res2 == 7L)
    }
}