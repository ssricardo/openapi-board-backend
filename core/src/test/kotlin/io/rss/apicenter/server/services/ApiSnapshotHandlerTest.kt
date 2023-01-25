package io.rss.apicenter.server.services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.rss.apicenter.server.persistence.entities.ApiRecord
import io.rss.apicenter.server.persistence.ApiNamespace
import io.rss.apicenter.server.persistence.dao.ApiRecordRepository
import io.rss.apicenter.server.persistence.dao.ApiSnapshotRepository
import io.rss.apicenter.server.persistence.entities.ApiSnapshot
import io.rss.apicenter.server.persistence.entities.ApiSnapshotId
import io.rss.apicenter.server.services.exceptions.BoardApplicationException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*
import javax.validation.Validation
import javax.validation.Validator

internal class ApiSnapshotHandlerTest {

    @Mock
    lateinit var repository: ApiSnapshotRepository
    @Mock
    lateinit var apiRecordRepository: ApiRecordRepository

    @InjectMocks
    lateinit var tested: ApiSnapshotHandler

    lateinit var validator: Validator

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.openMocks(this)
        val factory = Validation.buildDefaultValidatorFactory();
        validator = factory.validator;
    }

    @Test
    @DisplayName("Feed: app should have a name and namespace and version")
    internal fun feedEmptyPart() {
        val validationErrors = validator.validate(ApiRecord("", "", ""))
        assertEquals(3, validationErrors.size)
    }

    @Test
    internal fun feedOk() {
        tested.create(ApiRecord("MyApp", "SomeNmp", "1.0-SNAPSHOT").apply {
            source = "NOTHING"
            apiUrl = "http://google.com"
        })
    }

    @Test
    @Disabled("TO FIX")
    internal fun listVersionsByApp() {
        whenever(repository.findApiVersionList("GoApp", "Winterfell"))
                .thenReturn(listOf("1.0", "1.2", "2.0"))
        whenever(apiRecordRepository.findApiNamespace(any())).thenReturn(ApiNamespace("GoAp", "Winterfell"))

        val result: List<String> = tested.listVersionsByApi(UUID.randomUUID())
        assert(result.size == 3)
    }

    @Test
    @DisplayName("comparison requires valid AppSnapshots")
    internal fun createComparisonErrorNoApp() {
        whenever(repository.findById(ApiSnapshotId("name", "namespace", "7.0")))
                .thenReturn(Optional.empty())
        whenever(repository.findById(ApiSnapshotId("name", "namespace", "1.0")))
                .thenReturn(Optional.of(ApiSnapshot("name", "namespace", "2.0")))

        assertAll(
            {
                // finds none of them
                assertThrows(BoardApplicationException::class.java) {
                    tested.buildComparison(ApiSnapshotId("name", "namespace", "7.0"),
                            ApiSnapshotId("name", "namespace", "7.0"))
                }
            },
            {
                assertThrows(BoardApplicationException::class.java) {

                    // finds only one of them
                    tested.buildComparison(ApiSnapshotId("name", "namespace", "7.0"),
                            ApiSnapshotId("name", "namespace", "1.0"))
                }
            }
        )
    }

    @Test
    fun createComparison() {
        whenever(repository.findById(ApiSnapshotId("name", "namespace", "1.0")))
                .thenReturn(Optional.of(ApiSnapshot("name", "namespace", "1.1")))
        whenever(repository.findById(ApiSnapshotId("name", "other", "2.0")))
                .thenReturn(Optional.of(ApiSnapshot("name", "other", "1.1")))

        tested.buildComparison(ApiSnapshotId("name", "namespace", "1.0"),
                ApiSnapshotId("name", "other", "2.0"))
    }
}