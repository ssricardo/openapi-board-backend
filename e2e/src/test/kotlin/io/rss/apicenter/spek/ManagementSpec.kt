package io.rss.apicenter.spek

import io.rss.apicenter.DataProvider.givenApi
import io.rss.apicenter.DataProvider.givenNamespace
import io.rss.apicenter.RestRequestHelper
import io.rss.apicenter.RestRequestHelper.asListFromLastResponseContains
import io.rss.apicenter.RestRequestHelper.asListFromLastResponseNotInclude
import io.rss.apicenter.RestRequestHelper.`from last response equals`
import io.rss.apicenter.RestRequestHelper.getJson
import io.rss.apicenter.RestRequestHelper.givenBasicAuth
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.*

class ManagementSpec: Spek({

    Feature("User can list namespaces and APIs") {
        beforeFeature {
            givenBasicAuth(RestRequestHelper.USER_ADMIN)
        }

        Scenario("List successfuly") {
            Given("Namespaces exist") {
                listOf("brasil", "america", "europe", "asia")
                        .forEach (::givenNamespace)
            }

            When("Listing namespaces") {
                getJson("namespaces")
            }

            ThenResponseIsOK()
            Then("It contains item") {
                "$[*]" asListFromLastResponseContains "brasil"
            }
        }

        Scenario("User can list APIs in given namespace") {
            Given("APIs in distinct namespaces") {
                givenApi("hello", "america", "1.0")
                givenApi("bye", "europe", "1.0-SNAPSHOT")
                givenApi("skyApp", "brasil", "1.1")
                givenApi("skyApp", "brasil", "1.2")
                givenApi("ocean", "brasil", "2.0")
                givenApi("bigData", "asia", "1")
            }

            When("Search APIs in 'brasil' namespace") {
                getJson("apis?nm=brasil")
            }

            ThenResponseIsOK()
            Then("verify items") {
                "$.*.name" asListFromLastResponseContains "ocean"
                "$.*.name" asListFromLastResponseNotInclude "hello"
            }
        }

        Scenario("User sees the current version of an API") {
            val apiName = "skyApp".plus(UUID.randomUUID())
            lateinit var apiId: String
            Given("Distinct versions of API are pushed") {
                givenNamespace("repeatedNs")
                apiId = givenApi(apiName, "repeatedNs", "1.0") ?: "0"
            }
            And("New version is pushed") {
                givenApi(apiName, "repeatedNs", "1.2")
            }

            When("Gets API") {
                getJson("apis/$apiId")
            }

            ThenResponseIsOK()
            Then("Version should be 1.2") {
                "$.version" `from last response equals` "1.2"
            }
        }
    }


}) {}