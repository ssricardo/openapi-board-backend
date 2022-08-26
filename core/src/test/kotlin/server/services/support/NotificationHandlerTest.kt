package io.rss.openapiboard.server.services.support

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.config.EnvironmentConfig
import io.rss.openapiboard.server.helper.TokenHelper
import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import io.rss.openapiboard.server.persistence.dao.ApiSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.javamail.JavaMailSender
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService

@ExtendWith(MockitoExtension::class)
class NotificationHandlerTest {

    @Mock
    lateinit var apiSnapshotRepository: ApiSnapshotRepository

    @Mock
    lateinit var subscriptionRepository: AlertSubscriptionRepository

    @Mock
    lateinit var executorService: ExecutorService

    @Spy
    val envConfig = EnvironmentConfig("http://bla", true)

    @Mock
    private lateinit var emailSender: JavaMailSender

    @InjectMocks
    val tested = NotificationHandler()

    @BeforeEach
    fun setUp() {
        TokenHelper.setupAlgorithm("test")
    }

    @Test
    @Disabled("Not implemented yet")
    fun `no changes detected`() {
        TODO("Not yet implemented. Need to be done after changes detection implementation")
    }

    @Test
    fun `send changes OK`() {
        whenever(subscriptionRepository.findByApi(anyString())).thenReturn(
                listOf(AlertSubscription(1).apply {
                    apiName = "videos"
                    email = "ricardo@test.com"
                }, AlertSubscription(2).apply {
                    apiName = "videos"
                    email = "anna@test.com"
                })
        )
        tested.notifyUpdate(ApiRecord("videos","master")
                .apply {
                    version = "1.5"
                    lastModified = LocalDateTime.now()
                })
        verify(executorService, times(2)).submit(any())
    }
}