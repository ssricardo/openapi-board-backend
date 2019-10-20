package io.rss.openapiboard.server.persistence

import io.rss.openapiboard.server.persistence.dao.AppSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.persistence.entities.AppSnapshot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.inject.Inject
import javax.persistence.EntityManager

@ExtendWith(SpringExtension::class)
@DataJpaTest
class AppSnapshotRepositoryTest {

    @Inject
    lateinit var tested: AppSnapshotRepository

    @Inject
    lateinit var em: EntityManager

    @BeforeEach
    internal fun setUp() {
        tested.save(AppSnapshot("RicardoApp", "test", "1.0"))
        tested.save(AppSnapshot("RicardoApp", "test", "2.0"))
        tested.save(AppSnapshot("RicardoApp", "test", "3.0"))
        tested.save(AppSnapshot("DiffNS", "other", "5.0"))
        tested.save(AppSnapshot("Yes", "test", "2.0"))
    }

    @Test
    internal fun `find namespaces`() {
        val res = tested.findAppVersionList(AppRecordId("RicardoApp", "test"))
        assert(res.size == 3)
        assert("2.0" in res)
        assert("5.0" !in res)
    }

    @Test
    internal fun `insert and update`() {
        val res = tested.count()
        assert(res == 5L)
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.save(AppSnapshot("NewOne", "test", "3").apply {
            address = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })  // 4
        tested.save(AppSnapshot("Yes", "more", "3"))   // same
        tested.save(AppSnapshot("NewOne", "test", "3"))

        val res2 = tested.count()
        assert(res2 == 7L)
    }
}