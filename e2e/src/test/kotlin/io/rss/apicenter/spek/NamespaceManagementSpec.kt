package io.rss.apicenter.spek

import io.rss.apicenter.RestRequestHelper
import io.rss.apicenter.RestRequestHelper.deleteHttp
import io.rss.apicenter.RestRequestHelper.getFromResponse
import io.rss.apicenter.RestRequestHelper.givenBasicAuth
import io.rss.apicenter.RestRequestHelper.postJson
import io.rss.apicenter.RestRequestHelper.postJsonObject
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.http.HttpStatus

class NamespaceManagementSpec: Spek({

    Feature("As an application manager, I can view, create, removed Namespaces") {

        beforeEachScenario {
            givenBasicAuth(RestRequestHelper.USER_ADMIN)
        }

        Scenario("Create and remove simple Namespace") {
            When("Submit Namespace") {
                postJsonObject("namespaces",
                        mapOf("name" to "simpleNs"))
            }

            ThenResponseIsOK()

            When("Remove 'simpleNs'") {
                val namespaceId = getFromResponse<String>("$.id")
                deleteHttp("namespaces/$namespaceId")
            }

            ThenResponseIs(HttpStatus.NO_CONTENT)
        }

        Scenario("Create namespace with authorities") {
            When("Posting NS with authorities") {
                postJsonObject("namespaces",
                    mapOf(
                            "name" to "ns_restricted",
                            "authorities" to listOf("GREEN_PERM", "BLUE_PERM")
                    ))
            }

            ThenResponseIsOK()
        }

        Scenario("Name is mandatory") {
            When("") {
                postJson("namespaces", "")
            }

            ThenResponseIs(HttpStatus.CONFLICT)
        }
    }
})