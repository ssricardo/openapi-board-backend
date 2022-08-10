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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

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
    var tested = ApiRecordHandler()

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @DisplayName("must not save when missing fields")
    fun saveOrUpdateMissingData() {
        assertAll (
            {
                assertThrows(BoardApplicationException::class.java) { tested.createOrUpdate(ApiRecord()) }
            },
            {
                assertThrows(BoardApplicationException::class.java) {
                    tested.createOrUpdate(ApiRecord("SomeName", "SomeNS")) }
            }
        )
    }

    @Test
    fun saveOK() {
        val app = ApiRecord("test", "local").apply {
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