package io.rss.openapiboard.server.persistence

import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

class TestSpringProducer {

    companion object {

        @Component
        class Producer {
            @Bean
            fun testValidator(): javax.validation.Validator = mock(javax.validation.Validator::class.java)
        }

    }
}