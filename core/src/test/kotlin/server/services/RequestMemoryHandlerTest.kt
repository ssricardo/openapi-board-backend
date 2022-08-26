package io.rss.openapiboard.server.services

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.dao.ApiOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestMemoryRepository
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.request.RequestMemory
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.RequestMemoryRequestResponse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import javax.validation.Validator
import com.nhaarman.mockitokotlin2.any as some

@ExtendWith(MockitoExtension::class)
class RequestMemoryHandlerTest {

    @InjectMocks
    var tested = RequestMemoryHandler()

    @Mock
    private lateinit var requestRepository: RequestMemoryRepository

    @Mock
    private lateinit var operationRepository: ApiOperationRepository

    @Mock
    private lateinit var validator: Validator

    @Test
    fun testSaveOK() {
        whenever(operationRepository.findSingleMatch(some(), some(), some(), some())) doReturn
                ApiOperation(1)
        whenever(requestRepository.save( any(RequestMemory::class.java) )) doReturn RequestMemory()

        tested.saveRequest(RequestMemoryRequestResponse(
                namespace = "Prod", apiName = "testing", path = "/test", methodType = MethodType.POST
        ).apply { title = "Meu test app" })
    }

    @Test
    fun testSaveMissingField() {
        assertThrows(BoardApplicationException::class.java) {
            tested.saveRequest(RequestMemoryRequestResponse(
                    namespace = "Prod", apiName = "testing", path = "/test"
            ))
        }
    }
}