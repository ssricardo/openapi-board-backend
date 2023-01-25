package io.rss.apicenter.spek

import io.rss.apicenter.DataProvider.givenApi
import io.rss.apicenter.DataProvider.givenNamespace
import io.rss.apicenter.RestRequestHelper
import io.rss.apicenter.RestRequestHelper.asListFromLastResponseContains
import io.rss.apicenter.RestRequestHelper.asListFromLastResponseNotInclude
import io.rss.apicenter.RestRequestHelper.getFromResponse
import io.rss.apicenter.RestRequestHelper.getJson
import io.rss.apicenter.RestRequestHelper.givenBasicAuth
import io.rss.apicenter.RestRequestHelper.postJsonObject
import io.rss.apicenter.server.persistence.MethodType
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.http.HttpStatus

class RequiredAuthoritySpec: Spek({

    Feature("APIs and Namespaces can be restricted to specified Authorities") {

        beforeEachScenario {
            givenBasicAuth(RestRequestHelper.USER_ADMIN)
        }

        Scenario("User lists only Namespaces available to him") {

            Given("Namespaces with and without restriction and APIs") {
                givenNamespace("world")
                givenNamespace("blue-ns", listOf("BLUE_PERM"))
                givenNamespace("green-ns", listOf("GREEN_PERM"))
            }

            And("User with 'green' ROLE lists namespaces") {
                givenBasicAuth(RestRequestHelper.USER_GREEN)
            }

            When("User lists namespaces") {
                getJson("namespaces")
            }

            ThenResponseIsOK()

            Then("It only shows permitted namespaces") {
                "$[*]" asListFromLastResponseContains "world"
                "$[*]" asListFromLastResponseContains "green-ns"
                "$[*]" asListFromLastResponseNotInclude "blue-ns"
            }
        }

        Scenario("Lists only APIs available to user") {

            Given("Namespaces with and without restriction and APIs") {
                givenNamespace("blue-ns", listOf("BLUE_PERM"))
                givenApi("argentina", "blue-ns", "1.0")
                givenNamespace("green-ns", listOf("GREEN_PERM"))
                givenApi("brazil", "green-ns", "1.0")
            }

            And("User has 'green' ROLE") {
                givenBasicAuth(RestRequestHelper.USER_GREEN)
            }

            When("User lists namespaces") {
                getJson("apis?nm=green-ns")
            }

            ThenResponseIsOK()

            Then("It only shows permitted namespaces") {
                "$[*].name" asListFromLastResponseContains "brazil"
                "$[*].name" asListFromLastResponseNotInclude "argentina"
            }

        }

        Scenario("Users tries to access forbidden API and is denied") {

            Given("Namespaces with and without restriction and APIs") {
                givenNamespace("blue-ns", listOf("BLUE_PERM"))
                givenApi("argentina", "blue-ns", "1.0")
            }

            lateinit var apiId: String
            Given("API in blue-ns Namespace") {
                givenApi("Iceland", "blue-ns", "1.0")
                apiId = getFromResponse("$")!!
            }

            Given("User has 'green' ROLE") {
                givenBasicAuth(RestRequestHelper.USER_GREEN)
            }

            When("User tries to access Api in blue-ns") {
                getJson("apis/$apiId")
            }
            ThenResponseIs(HttpStatus.FORBIDDEN)

            Given("New API in user namespace but distinct auth requirement") {
                givenBasicAuth(RestRequestHelper.USER_ADMIN)
                givenNamespace("green-ns", listOf("GREEN_PERM"))
                givenApi("brazil", "green-ns", "1.0")
                givenApi("greenland", "green-ns", "1.0", "BLUE_PERM")
                apiId = getFromResponse("$")!!
            }

            When("User with 'green' ROLE tries to access API in 'green-ns' but restricted to 'BLUE_PERM'") {
                givenBasicAuth(RestRequestHelper.USER_GREEN)
                getJson("apis/$apiId")
            }

            ThenResponseIs(HttpStatus.FORBIDDEN)
        }

        Scenario("User has access to API but not to the request sample") {

            Given("namespace and API") {
                givenNamespace("SamplerNs")
                givenApi("sample-api", "SamplerNs", "1.0")
                Thread.sleep(500)   // Wait operation / source to be processed
            }

            Given("Request sample without restriction") {
                val request = mutableMapOf(
                        "namespace" to "SamplerNs",
                        "apiName" to "sample-api",
                        "path" to "/pets",
                        "methodType" to MethodType.POST,
                        "title" to "SampleForAll",
                        "body" to "something=nothing",
                )

                postJsonObject("requests", request)
            }
            And("Other request sample restricted to GREEN") {
                val request = mutableMapOf(
                        "namespace" to "SamplerNs",
                        "apiName" to "sample-api",
                        "path" to "/pets",
                        "methodType" to MethodType.POST,
                        "title" to "sampleGreenTest",
                        "body" to "something=test",
                        "requiredAuthorities" to listOf("GREEN_PERM")
                )

                postJsonObject("requests", request)
            }

            Given("User with Blue Role") {
                givenBasicAuth(RestRequestHelper.USER_BLUE)
            }

            When("Lists request samples") {
                getJson("requests?q=samp")
            }

            Then("It does not show previous request") {
                "$.result[*].title" asListFromLastResponseContains "SampleForAll"
                "$.result[*].title" asListFromLastResponseNotInclude "sampleGreenTest"
            }
        }
    }


}) {}