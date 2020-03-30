package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.persistence.dao.AppOperationRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations


internal class SideOperationsProcessorTest {

    @InjectMocks
    val tested = SideOperationsProcessor()

    @Mock
    lateinit var operationRepository: AppOperationRepository

    @BeforeEach
    internal fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    internal fun processSourceOk() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    internal fun processBadSource() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    internal fun processEmptySource() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}