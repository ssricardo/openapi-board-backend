package io.rss.apicenter.server.persistence

import io.rss.apicenter.server.persistence.dao.NamespaceRepository
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.annotation.Resource

@DataJpaTest()
@Tag("db")
class NamespaceRepositoryTest {

    @Resource
    lateinit var underTest: NamespaceRepository

    @Test
    fun `list namespaces`() {
        underTest.findAll()
    }

    @Test
    fun `list with authorities`() {
        underTest.findAllWithAuthorities()
    }
}