package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.AppRecordRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertAll
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

internal class AppRecordHandlerTest {

    @Mock
    lateinit var repository: AppRecordRepository

    @Mock
    lateinit var snapshotService: AppSnapshotHandler

    @Mock
    lateinit var appSourceProcessor: AppSourceProcessor

    @InjectMocks
    var tested = AppRecordHandler()

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @DisplayName("must not save when missing fields")
    fun saveOrUpdateMissingData() {
        assertAll (
            {
                assertThrows(BoardApplicationException::class.java) { tested.createOrUpdate(AppRecord()) }
            },
            {
                assertThrows(BoardApplicationException::class.java) {
                    tested.createOrUpdate(AppRecord("SomeName", "SomeNS")) }
            }
        )
    }

    @Test
    fun saveOK() {
        val app = AppRecord("test", "local").apply {
            version = "1.0"
            source = "{}"
            address = "http://google.com"
        }
        `when`(repository.saveAndFlush(app)).thenReturn(app)

        tested.createOrUpdate(app)

        verify(repository, times(1)).saveAndFlush(app)
        verify(snapshotService, times(1)).feed(app)
    }
}