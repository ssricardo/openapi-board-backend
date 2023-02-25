package io.rss.apicenter.spek

import io.rss.apicenter.DataProvider.givenApi
import io.rss.apicenter.RestRequestHelper
import io.rss.apicenter.RestRequestHelper.deleteHttp
import io.rss.apicenter.RestRequestHelper.getFromResponse
import io.rss.apicenter.RestRequestHelper.postJsonObject
import org.junit.platform.commons.PreconditionViolationException
import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.Skip
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.http.HttpStatus
import java.util.UUID

class NotificationSpec: Spek({

    Feature("A user can subscribe and un-subscribe to an API and receive notifications") {

        lateinit var apiId: String

        beforeFeature {
            RestRequestHelper.givenBasicAuth(RestRequestHelper.USER_ADMIN)
                apiId = givenApi("HowIChangeAPI", "Series", "1.0")
                    ?: throw PreconditionViolationException("API ID is required")
        }

        Scenario("User register for notification") {
            When("Post new subscription") {
                postJsonObject("subscriptions", mapOf(
                    "hookAddress" to "https://webhook-test.com",
                    "apiName" to "HowIChangeAPI",
                    "namespace" to "Series",
                    "basePathList" to listOf("/pets")
                ))
            }

            ThenResponseIs(HttpStatus.CREATED)
        }

        Scenario("User can remove subscription") {

            Given("Subscription is registered") {
                postJsonObject("subscriptions", mapOf(
                    "hookAddress" to "https://webhook-test.com",
                    "apiName" to "AnSubsToDelete",
                    "namespace" to "Series",
                ))

            }

            When("Tries to remove") {
                val subsId = getFromResponse<Int>("$.id")
                deleteHttp("subscriptions/$subsId")
            }

            ThenResponseIs(HttpStatus.NO_CONTENT)
        }

        Scenario("There is a subscription and new version of API is submitted") {
            Skip.Yes("Not implemented")
        }
    }


}) {}