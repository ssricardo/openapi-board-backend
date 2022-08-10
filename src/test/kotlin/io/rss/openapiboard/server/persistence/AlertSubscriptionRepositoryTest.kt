package io.rss.openapiboard.server.persistence

import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Tag("db")
class AlertSubscriptionRepositoryTest {

    @Inject
    lateinit var tested: AlertSubscriptionRepository

    @Test
    fun testFindByApp() {
        val res = tested.findByApi("books")
        assertNotNull(res)
    }
}