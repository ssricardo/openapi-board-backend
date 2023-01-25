package io.rss.openapiboard.spek

import io.rss.openapiboard.DataProvider.givenApi
import io.rss.openapiboard.DataProvider.givenNamespace
import io.rss.openapiboard.RestRequestHelper
import io.rss.openapiboard.RestRequestHelper.getFromResponse
import io.rss.openapiboard.RestRequestHelper.givenBasicAuth
import io.rss.openapiboard.RestRequestHelper.postJsonObject
import io.rss.openapiboard.RestRequestHelper.putJson
import io.rss.openapiboard.server.persistence.MethodType
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.http.HttpStatus

class RequestSampleSpec: Spek({

    Feature("User can store and retrieve samples of requests, with headers and content") {

        beforeFeature {
            givenBasicAuth(RestRequestHelper.USER_ADMIN)
        }

        Scenario("User can create and update existing request") {
            lateinit var apiId: String
            Given("Namespace and API") {
                givenNamespace("testing")
                apiId = givenApi("memo", "testing", "1.0") ?: "0"
            }

            val request = mutableMapOf(
                    "namespace" to "testing",
                    "apiName" to "memo",
                    "path" to "/pets",
                    "methodType" to MethodType.POST,
                    "title" to "Feature test request",
                    "body" to "something=test",
                    "parameters" to listOf(
                            mapOf("kind" to "HEADER", "name" to "contentType", "value" to "text/plain")
                    )
            )

            When("Post new sample") {
                postJsonObject("requests", request)
            }
            ThenResponseIs(HttpStatus.CREATED)

            When("User updates sample") {
                val newRequest = request.apply {
                    "title" to "Changed request"
                }
                val requestId: Int? = getFromResponse("$.requestId")
                putJson("requests/$requestId", newRequest)
            }

            ThenResponseIsOK()
        }
    }
}) {}