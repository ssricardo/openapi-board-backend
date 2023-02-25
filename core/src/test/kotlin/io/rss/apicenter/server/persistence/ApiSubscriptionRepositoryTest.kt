package io.rss.apicenter.server.persistence

import io.rss.apicenter.server.persistence.dao.ApiSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.Resource

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class ApiSubscriptionRepositoryTest {

    @Resource
    lateinit var tested: ApiSubscriptionRepository

    @Test
    fun testFindByApp() {
        val res = tested.findByApi("books")
        assertNotNull(res)
    }

    @Test
    internal fun testFindByApiMail() {
        val res = tested.findByHookApi("http://test.my.hook", "books")
        assertNull(res)
    }
}