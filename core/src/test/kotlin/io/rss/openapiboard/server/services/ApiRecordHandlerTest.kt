package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.ApiRecordRepository
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

    @InjectMocks
    lateinit var tested: ApiRecordHandler

    @Test
    @DisplayName("must not save when missing fields")
    fun saveOrUpdateMissingData() {
        assertAll (
            {
                assertThrows(BoardApplicationException::class.java) {
                    tested.createOrUpdate(ApiRecord("SomeName", "", "")) }
            }
        )
    }

    @Test
    fun saveOK() {
        val app = ApiRecord("test", "local", "2.0").apply {
            version = "1.0"
            source = "{}"
            apiUrl = "http://google.com"
        }
        `when`(repository.saveAndFlush(app)).thenReturn(app)

        tested.createOrUpdate(app)

        verify(repository, times(1)).saveAndFlush(app)
        verify(snapshotService, times(1)).create(app)
    }
}