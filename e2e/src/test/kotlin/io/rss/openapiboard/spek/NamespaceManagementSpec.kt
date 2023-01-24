package io.rss.openapiboard.spek

import io.rss.openapiboard.RestRequestHelper
import io.rss.openapiboard.RestRequestHelper.deleteHttp
import io.rss.openapiboard.RestRequestHelper.getFromResponse
import io.rss.openapiboard.RestRequestHelper.givenBasicAuth
import io.rss.openapiboard.RestRequestHelper.postJson
import io.rss.openapiboard.RestRequestHelper.postJsonObject
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.http.HttpStatus

class NamespaceManagementSpec: Spek({

    Feature("As an application manager, I can view, create, removed Namespaces") {

        beforeFeature {
            givenBasicAuth(RestRequestHelper.USER_ADMIN)
        }

        Scenario("Create and remove simple Namespace") {
            When("Submit Namespace") {
                postJsonObject("namespaces",
                        mapOf("name" to "simpleNs"))
            }

            ThenResponseIsOK()

            When("Remove 'simpleNs'") {
                val namespaceId = getFromResponse<String>("$.name")
                deleteHttp("namespaces/$namespaceId")
            }

            ThenResponseIs(HttpStatus.NO_CONTENT)
        }

        Scenario("Create namespace with authorities") {
            When("Posting NS with authorities") {
                postJsonObject("namespaces",
                    mapOf(
                            "name" to "ns_restricted",
                            "authorities" to listOf("ROLE_TMT", "ROLE_PEN_TEST")
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