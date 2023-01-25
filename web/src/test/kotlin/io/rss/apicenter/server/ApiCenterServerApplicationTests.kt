package io.rss.apicenter.server

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ApiCenterServerApplicationTests.TestConfig::class, ApiCenterServerApplication::class])
@Disabled
class ApiCenterServerApplicationTests {

	@Configuration
	companion object class TestConfig {

//		@Bean
//		fun mailSender(): ApiRecordHandler {
//			return ApiRecordHandler()
//		}
	}

	@Test
	fun contextLoads() {
	}

}
