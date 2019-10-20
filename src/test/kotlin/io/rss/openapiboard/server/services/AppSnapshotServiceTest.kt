package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.AppSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.persistence.entities.AppSnapshot
import io.rss.openapiboard.server.persistence.entities.AppSnapshotId
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

internal class AppSnapshotServiceTest {

    @Mock
    lateinit var repository: AppSnapshotRepository

    @InjectMocks
    var tested = AppSnapshotService()

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @DisplayName("Feed: app should have a name and namespace and version")
    internal fun feedNullPart() {
        assertAll(
                {
                    assertThrows(AssertionError::class.java) {
                        tested.feed(AppRecord())
                    }
                },
                {
                    assertThrows(AssertionError::class.java) {
                        tested.feed(AppRecord("Test", "MyNm"))
                    }
                }
        )

    }

    @Test
    internal fun feedOk() {
        tested.feed(AppRecord("MyApp", "SomeNmp").apply {
            version = "1.0-SNAPSHOT"
            source = "NOTHING"
            address = "http://google.com"
        })
    }

    @Test
    @Disabled("TO FIX")
    internal fun listVersionsByApp() {
        Mockito.`when`(repository.findAppVersionList(AppRecordId("GoApp", "Winterfell")))
                .thenReturn(listOf("1.0", "1.2", "2.0"))
        val result: List<String> = tested.listVersionsByAppNamespace("GoAp", "Winterfell")
        assert(result.size == 3)
    }

    @Test
    @DisplayName("comparison requires valid AppSnapshots")
    internal fun createComparisonErrorNoApp() {
        Mockito.`when`(repository.findById(AppSnapshotId("name", "namespace", "7.0")))
                .thenReturn(Optional.empty())
        Mockito.`when`(repository.findById(AppSnapshotId("name", "namespace", "1.0")))
                .thenReturn(Optional.of(AppSnapshot("name", "namespace")))

        assertAll(
            {
                // finds none of them
                assertThrows(BoardApplicationException::class.java) {
                    tested.createComparison(AppSnapshotId("name", "namespace", "7.0"),
                            AppSnapshotId("name", "namespace", "7.0"))
                }
            },
            {
                assertThrows(BoardApplicationException::class.java) {

                    // finds only one of them
                    tested.createComparison(AppSnapshotId("name", "namespace", "7.0"),
                            AppSnapshotId("name", "namespace", "1.0"))
                }
            }
        )
    }

    @Test
    internal fun createComparison() {
        Mockito.`when`(repository.findById(AppSnapshotId("name", "namespace", "1.0")))
                .thenReturn(Optional.of(AppSnapshot("name", "namespace")))
        Mockito.`when`(repository.findById(AppSnapshotId("name", "other", "2.0")))
                .thenReturn(Optional.of(AppSnapshot("name", "other")))

        val res = tested.createComparison(AppSnapshotId("name", "namespace", "1.0"),
                AppSnapshotId("name", "other", "2.0"))
    }
}