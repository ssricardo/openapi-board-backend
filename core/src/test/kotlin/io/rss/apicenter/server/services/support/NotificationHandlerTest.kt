package io.rss.apicenter.server.services.support

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.rss.apicenter.server.config.EnvironmentConfig
import io.rss.apicenter.server.helper.TokenHelper
import io.rss.apicenter.server.persistence.dao.AlertSubscriptionRepository
import io.rss.apicenter.server.persistence.dao.ApiSnapshotRepository
import io.rss.apicenter.server.persistence.entities.AlertSubscription
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.mail.javamail.JavaMailSender
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
    lateinit var underTest: NotificationHandler

    @BeforeEach
    fun setUp() {
        TokenHelper.setupAlgorithm("test")
        whenever(apiSnapshotRepository.findTopPreviousVersion(anyString(), anyString(), anyString(), any()))
                .thenReturn(Page.empty())
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
        underTest.notifyUpdate(ApiRecord("videos","master", "1.5")
                .apply {
                    updateDate()
                })
        verify(executorService, times(2)).submit(any())
    }
}