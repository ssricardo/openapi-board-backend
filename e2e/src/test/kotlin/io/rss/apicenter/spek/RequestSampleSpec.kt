package io.rss.apicenter.spek

import io.rss.apicenter.DataProvider.givenApi
import io.rss.apicenter.DataProvider.givenNamespace
import io.rss.apicenter.RestRequestHelper
import io.rss.apicenter.RestRequestHelper.getFromResponse
import io.rss.apicenter.RestRequestHelper.givenBasicAuth
import io.rss.apicenter.RestRequestHelper.postJsonObject
import io.rss.apicenter.RestRequestHelper.putJson
import io.rss.apicenter.server.persistence.MethodType
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.http.HttpStatus

class RequestSampleSpec: Spek({

    Feature("User can store and retrieve samples of requests, with headers and content") {

        beforeFeature {
            givenBasicAuth(RestRequestHelper.USER_ADMIN)
        }

        Scenario("User can create and update existing request") {
            Given("Namespace and API") {
                givenNamespace("testing")
                givenApi("memo", "testing", "1.0") ?: "0"
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