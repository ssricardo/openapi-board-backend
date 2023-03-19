package io.rss.apicenter.server.services.support

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.rss.apicenter.server.config.EnvironmentConfig
import io.rss.apicenter.server.persistence.dao.ApiSubscriptionRepository
import io.rss.apicenter.server.persistence.dao.ApiSnapshotRepository
import io.rss.apicenter.server.persistence.entities.ApiSubscription
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import javax.ws.rs.client.Client

@ExtendWith(MockitoExtension::class)
class NotificationHandlerTest {

    @Mock
    lateinit var apiSnapshotRepository: ApiSnapshotRepository

    @Mock
    lateinit var subscriptionRepository: ApiSubscriptionRepository

    @Mock
    lateinit var executorService: ExecutorService

    @Spy
    val envConfig = EnvironmentConfig("http://bla", true)

    @Mock
    lateinit var restClient: Client

    lateinit var underTest: NotificationHandler

    @BeforeEach
    fun setUp() {
        whenever(apiSnapshotRepository.findTopPreviousVersion(anyString(), anyString(), anyString(), any()))
                .thenReturn(Page.empty())
        whenever(executorService.submit(any())).thenAnswer {
            val runnable: Runnable = it.getArgument(0)
            runnable.run()
            Thread.sleep(500)
            CompletableFuture.completedFuture("Ok")
        }

        underTest = NotificationHandler(apiSnapshotRepository, subscriptionRepository, executorService, envConfig, restClient)
    }

    @Test
    @Disabled("Not implemented yet")
    fun `no changes detected`() {
        TODO("Not yet implemented. Need to be done after changes detection implementation")
    }

    @Test
    fun `send changes OK`() {
        whenever(subscriptionRepository.findByApi(anyString())).thenReturn(
                listOf(ApiSubscription(1).apply {
                    apiName = "videos"
                    targetWebhook = "http://other-service"
                }, ApiSubscription(2).apply {
                    apiName = "videos"
                    targetWebhook = "http://http-mail"
                })
        )

        underTest.notifyUpdateAsync(ApiRecord("videos","master", "1.5")
                .apply {
                    updateDate()
                })

        verify(executorService, times(2)).submit(any())
        verify(restClient, times(1)).target("http://other-service")
        verify(restClient, times(1)).target("http://http-mail")
    }
}