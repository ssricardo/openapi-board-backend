package io.rss.apicenter.server.persistence

import io.rss.apicenter.server.persistence.dao.ApiRecordRepository
import io.rss.apicenter.server.persistence.entities.ApiAuthority
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import javax.annotation.Resource

@DataJpaTest
@Tag("db")
class ApiRecordRepositoryTest {

    @Resource
    private lateinit var tested: ApiRecordRepository

    private val insertedRecords = mutableListOf<ApiRecord>()

    @BeforeEach
    internal fun setUp() {
        tested.save(ApiRecord("RicardoApp", "test", "v1")).also { insertedRecords.add(it) }
        tested.save(ApiRecord("DiffNS", "other", "v1")).also { insertedRecords.add(it) }
        tested.save(ApiRecord("Yes", "test", "v1")).also { insertedRecords.add(it) }
    }

    @Test
    internal fun `find all on namespace`() {
        val res = tested.findApiVersionByNamespace("test")
        assert(res.size == 2)
    }

    @Test
    fun `insert and update`() {
        val res = tested.count()
        assert(res == 3L)
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()
        tested.save(ApiRecord("NewOne", "test", "v3").apply {
            apiUrl = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        })  // 4
        val yesMore = tested.findByNamespaceName("test", "Yes")
        yesMore?.let {
            it.version = "v3"
            tested.save(it)
        }

        val res2 = tested.count()
        assertEquals(4L, res2)
    }

    @Test
    internal fun `get with sources`() {
        val fileContent = javaClass
                .getResource("/test-data/petstore-expanded.yaml")
                .readText()

        val entity = ApiRecord("NewOne", "test", "v3").apply {
            apiUrl = "http://someadress.on.internet:8080/someContext/onSubContext"
            source = fileContent
        }
        val inserted = tested.saveAndFlush(entity)

        val res = tested.findByIdOrNull(inserted.id)
        assertNotNull(res)
        assertEquals(fileContent, res?.source)
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

    @Test
    fun `find ids without access`() {
        val noRestriction = tested.saveAndFlush(ApiRecord("FreeWilly", "test", "v1"))
        val restrictedToUser = tested.saveAndFlush(ApiRecord("ApiIdk", "test", "v1").apply {
            this.requiredAuthorities = listOf("ROLE_IDK").map { ApiAuthority(this, it) }
        })
        val allowedToUser = tested.saveAndFlush(ApiRecord("ComeIn", "test", "v1").apply {
            this.requiredAuthorities = listOf("ROLE_TEST").map { ApiAuthority(this, it) }
        })

        val idList = listOf(noRestriction, restrictedToUser, allowedToUser).map { it.id!! }
        val result = tested.findDeniedApisForAuthorities(
                idList,
                listOf("ROLE_TEST")
        )

        assert(result.any { it == restrictedToUser.id })
        assert(result.none { it == noRestriction.id })
        assert(result.none { it == allowedToUser.id })
    }
}