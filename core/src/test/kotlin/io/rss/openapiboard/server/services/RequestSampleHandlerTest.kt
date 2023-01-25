package io.rss.openapiboard.server.services

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.dao.ApiOperationRepository
import io.rss.openapiboard.server.persistence.dao.RequestSampleRepository
import io.rss.openapiboard.server.persistence.entities.ApiOperation
import io.rss.openapiboard.server.persistence.entities.ApiRecord
import io.rss.openapiboard.server.persistence.entities.request.RequestSample
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.SampleRequestResponse
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
class RequestSampleHandlerTest {

    @InjectMocks
    lateinit var tested: RequestSampleHandler

    @Mock
    private lateinit var requestRepository: RequestSampleRepository

    @Mock
    private lateinit var operationRepository: ApiOperationRepository

    @Mock
    lateinit var namespaceHandler: NamespaceHandler

    @Mock
    private lateinit var validator: Validator

    @Test
    fun testSaveOK() {
        val operation = ApiOperation(ApiRecord("name", "ns", "v1"), 1)
        whenever(operationRepository.findSingleMatch(some(), some(), some(), some())) doReturn operation
        whenever(requestRepository.save( any(RequestSample::class.java) )) doReturn RequestSample(operation)

        tested.saveRequest(SampleRequestResponse(
                namespace = "Prod", apiName = "testing", path = "/test", methodType = MethodType.POST
        ).apply { title = "Meu test app" })
    }

    @Test
    fun testSaveMissingField() {
        assertThrows(BoardApplicationException::class.java) {
            tested.saveRequest(SampleRequestResponse(
                    namespace = "Prod", apiName = "testing", path = "/test"
            ))
        }
    }
}