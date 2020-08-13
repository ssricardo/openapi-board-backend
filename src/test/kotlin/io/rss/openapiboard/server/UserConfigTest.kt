package io.rss.openapiboard.server

import io.rss.openapiboard.server.security.config.UserConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ContextConfiguration


@SpringBootTest()
@ContextConfiguration(classes = [UserConfigTest.Companion.TestConfig::class])
@Disabled("TODO")
class UserConfigTest {

    companion object {

        @Configuration
        @ComponentScan(basePackageClasses = [UserConfig::class], includeFilters =
            [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes=[UserConfig::class])])
//        @ImportResource("/application.yml")
        class TestConfig {
        }

    }

    @Autowired
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