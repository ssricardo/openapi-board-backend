package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.ApiSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.ApiRecordId
import io.rss.openapiboard.server.persistence.entities.ApiSnapshot
import io.rss.openapiboard.server.persistence.entities.ApiSnapshotId
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.*

internal class ApiSnapshotHandlerTest {

    @Mock
    lateinit var repository: ApiSnapshotRepository

    @InjectMocks
    var tested = ApiSnapshotHandler()

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
                        tested.create(ApiRecord())
                    }
                },
                {
                    assertThrows(AssertionError::class.java) {
                        tested.create(ApiRecord("Test", "MyNm"))
                    }
                }
        )

    }

    @Test
    internal fun feedOk() {
        tested.create(ApiRecord("MyApp", "SomeNmp").apply {
            version = "1.0-SNAPSHOT"
            source = "NOTHING"
            apiUrl = "http://google.com"
        })
    }

    @Test
    @Disabled("TO FIX")
    internal fun listVersionsByApp() {
        Mockito.`when`(repository.findApiVersionList(ApiRecordId("GoApp", "Winterfell")))
                .thenReturn(listOf("1.0", "1.2", "2.0"))
        val result: List<String> = tested.listVersionsByApiNamespace("GoAp", "Winterfell")
        assert(result.size == 3)
    }

    @Test
    @DisplayName("comparison requires valid AppSnapshots")
    internal fun createComparisonErrorNoApp() {
        Mockito.`when`(repository.findById(ApiSnapshotId("name", "namespace", "7.0")))
                .thenReturn(Optional.empty())
        Mockito.`when`(repository.findById(ApiSnapshotId("name", "namespace", "1.0")))
                .thenReturn(Optional.of(ApiSnapshot("name", "namespace")))

        assertAll(
            {
                // finds none of them
                assertThrows(BoardApplicationException::class.java) {
                    tested.createComparison(ApiSnapshotId("name", "namespace", "7.0"),
                            ApiSnapshotId("name", "namespace", "7.0"))
                }
            },
            {
                assertThrows(BoardApplicationException::class.java) {

                    // finds only one of them
                    tested.createComparison(ApiSnapshotId("name", "namespace", "7.0"),
                            ApiSnapshotId("name", "namespace", "1.0"))
                }
            }
        )
    }

    @Test
    fun createComparison() {
        Mockito.`when`(repository.findById(ApiSnapshotId("name", "namespace", "1.0")))
                .thenReturn(Optional.of(ApiSnapshot("name", "namespace")))
        Mockito.`when`(repository.findById(ApiSnapshotId("name", "other", "2.0")))
                .thenReturn(Optional.of(ApiSnapshot("name", "other")))

        tested.createComparison(ApiSnapshotId("name", "namespace", "1.0"),
                ApiSnapshotId("name", "other", "2.0"))
    }
}