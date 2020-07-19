package io.rss.openapiboard.server.services.support

import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.config.EnvironmentConfig
import io.rss.openapiboard.server.persistence.dao.AlertSubscriptionRepository
import io.rss.openapiboard.server.persistence.dao.AppRecordRepository
import io.rss.openapiboard.server.persistence.entities.AlertSubscription
import io.rss.openapiboard.server.persistence.entities.AppRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.javamail.JavaMailSender
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import javax.mail.internet.MimeMessage

@ExtendWith(MockitoExtension::class)
class NotificationHandlerTest {

    @Mock
    lateinit var appRepository: AppRecordRepository

    @Mock
    lateinit var subscriptionRepository: AlertSubscriptionRepository

    @Mock
    lateinit var executorService: ExecutorService

    @Mock
    lateinit var envConfig: EnvironmentConfig

    @Mock
    private lateinit var emailSender: JavaMailSender

    @InjectMocks
    val tested = NotificationHandler()

    @BeforeEach
    internal fun setUp() {
        whenever(envConfig.serverAddress).thenReturn("http://testhost")
    }

    @Test
    @Disabled("Not implemented yet")
    fun `no changes detected`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `send changes OK`() {
        whenever(subscriptionRepository.findByApp(anyString())).thenReturn(
                listOf(AlertSubscription(1).apply {
                    appName = "videos"
                    email = "ricardo@test.com"
                }, AlertSubscription(2).apply {
                    appName = "videos"
                    email = "anna@test.com"
                })
        )
        tested.notifyUpdate(AppRecord("videos","master")
                .apply { lastModified = LocalDateTime.now() })
        verify(executorService, times(2)).submit(any())
    }
}