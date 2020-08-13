package io.rss.openapiboard.server

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [OpenapiBoardServerApplicationTests.TestConfig::class, OpenapiBoardServerApplication::class])
class OpenapiBoardServerApplicationTests {

	@Configuration
	companion object class TestConfig {

		@Bean
		fun mailSender(): JavaMailSender {
			return JavaMailSenderImpl()
		}
	}

	@Test
	fun contextLoads() {
	}

}
