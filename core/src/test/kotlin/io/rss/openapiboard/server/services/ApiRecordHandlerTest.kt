package io.rss.openapiboard.server.services

import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
import io.rss.openapiboard.server.persistence.dao.NamespaceRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.support.NotificationHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ApiRecordHandlerTest {

    @Mock
    lateinit var repository: ApiRecordRepository
    @Mock
    lateinit var snapshotService: ApiSnapshotHandler
    @Mock
    lateinit var apiSourceProcessor: ApiSourceProcessor
    @Mock
    lateinit var notificationHandler: NotificationHandler
    @Mock
    lateinit var namespaceHandler: NamespaceHandler

    lateinit var tested: ApiRecordHandler

    @BeforeEach
    internal fun setUp() {
        tested = ApiRecordHandler(repository, namespaceHandler, snapshotService, apiSourceProcessor, notificationHandler)
    }

    @Test
    fun `it must not save when missing fields`() {
        assertAll (
            {
                assertThrows(BoardApplicationException::class.java) {
                    tested.createOrUpdate(ApiRecord("SomeName", "", "")) }
            }
        )
    }

    @Test
    fun saveOK() {
        whenever(namespaceHandler.exists("local")).thenReturn(true)
        val app = ApiRecord("test", "local", "2.0").apply {
            source = "{}"
            apiUrl = "http://google.com"
        }
        whenever(repository.saveAndFlush(app)).thenReturn(app)

        tested.createOrUpdate(app)

        verify(repository, times(1)).saveAndFlush(app)
        verify(snapshotService, times(1)).create(app)
    }

    @Test
    fun `when namespace not exist`() {
        whenever(namespaceHandler.exists(anyString())).thenReturn(false)
        val app = ApiRecord("test", "namespace", "2.0").apply {
            source = "{}"
            apiUrl = "http://google.com"
        }

        assertThrows(BoardApplicationException::class.java) { tested.createOrUpdate(app) }
    }

    @Test
    fun `when auto-create namespace enabled`() {
        tested = ApiRecordHandler(repository, namespaceHandler, snapshotService, apiSourceProcessor, notificationHandler, true)
        whenever(namespaceHandler.exists(anyString())).thenReturn(false)
        val app = ApiRecord("test", "namespace", "2.0").apply {
            source = "{}"
            apiUrl = "http://google.com"
        }
        whenever(repository.saveAndFlush(app)).thenReturn(app)

        tested.createOrUpdate(app)

        verify(repository, times(1)).saveAndFlush(app)
    }
}