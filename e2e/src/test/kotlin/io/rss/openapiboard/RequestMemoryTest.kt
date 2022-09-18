package io.rss.openapiboard

import io.rss.openapiboard.DataProvider.givenApi
import io.rss.openapiboard.TestRequestHelpers.`POST json`
import io.rss.openapiboard.TestRequestHelpers.`PUT json`
import io.rss.openapiboard.TestRequestHelpers.`assert response is OK`
import io.rss.openapiboard.TestRequestHelpers.getFromResponse
import io.rss.openapiboard.TestRequestHelpers.toJson
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import io.rss.openapiboard.server.services.to.ParameterMemoryTO
import io.rss.openapiboard.server.services.to.RequestMemoryRequestResponse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@DisplayName("Users can create and maintain samples of requests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestMemoryTest {

    @BeforeAll
    fun beforeAll() {
        givenApi("memo", "testing", "1.0")
    }

    @Test
    @Disabled("Needs to be fixed")
    fun `user can create and update RequestMemory`() {
        Thread.sleep(1000)  // time for async process of api
        val payload = RequestMemoryRequestResponse(namespace = "testing", apiName = "memo", path = "  /pets", methodType = MethodType.POST).apply {
            title = "Feature test request"
            body = "something=test"
            parameters.add(ParameterMemoryTO(kind = ParameterType.HEADER, name = "contentType", value = "text/plain"))
        }

        `POST json`("requests",
                payload.toJson())
        val requestId: String? = getFromResponse("/id")

        `PUT json`("request/$requestId", payload.apply {
            title = "Changed request"
        }.toJson())

        `assert response is OK`()
    }

    @Test
    fun `user can remove a RequestMemory`() {
    }
}