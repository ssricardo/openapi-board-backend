package io.rss.openapiboard.server

import io.rss.openapiboard.server.security.config.UserConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.boot.test.context.SpringBootTest
import javax.inject.Inject

@SpringBootTest
class UserConfigTest {

    @Inject
    lateinit var config: UserConfig

    @Test
    internal fun verifyUsersRoles() {
        assertAll({
            assert(config.entries.map { it.name }.containsAll(arrayListOf("admin", "agent")))
        }, {
            assert(!config.entries.map { it.name }.contains("cheater"))
        }, {
            assert(config.entries[0].roles.containsAll(listOf("manager", "super saiyan")))
        }, {
            assert(config.entries[0].password == "admin00")
        })
    }
}