package io.rss.openapiboard.spek

import io.rss.openapiboard.DataProvider.givenApi
import io.rss.openapiboard.DataProvider.givenNamespace
import io.rss.openapiboard.RestRequestHelper
import io.rss.openapiboard.RestRequestHelper.`from last response equals`
import io.rss.openapiboard.RestRequestHelper.getJson
import io.rss.openapiboard.RestRequestHelper.givenBasicAuth
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class ComparisonSpec: Spek({

    Feature("Compare two APIs") {

        Scenario("User can compare APIs successfully") {

            beforeScenario {
                givenBasicAuth(RestRequestHelper.USER_ADMIN)
            }

            Given("Namespaces left & right") {
                givenNamespace("left")
                givenNamespace("right")
            }

            Given("API in namespace left") {
                givenApi("pets-store", "left", "1.0")
                givenApi("pets-store", "right", "1.7")
            }

            val api1 = "&srcName=pets-store&srcNs=left&srcVersion=1.0"
            val api2 = "&compareName=pets-store&compareNs=right&compareVersion=1.7"

            When("Gets a comparison") {
                getJson("apis/comparison?q=1$api1$api2")
            }

            Then("Result contains data from both APIs") {
                "$.source.name" `from last response equals` "pets-store"
                "$.source.namespace" `from last response equals` "left"
                "$.compared.namespace" `from last response equals` "right"
            }
        }
    }
}) {}