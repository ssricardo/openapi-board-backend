package io.rss.apicenter.spek

import io.rss.apicenter.RestRequestHelper
import io.rss.apicenter.RestRequestHelper.givenBasicAuth
import io.rss.apicenter.RestRequestHelper.putMultipartForm
import org.junit.jupiter.api.DisplayName
import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.Skip
import org.spekframework.spek2.style.gherkin.Feature
import org.springframework.util.LinkedMultiValueMap
import java.util.*

@DisplayName("How agent apps interact with this system")
class AgentPublishingSpec: Spek({

    Feature("Agent apps interact with this system") {

        beforeFeature {
            givenBasicAuth(RestRequestHelper.USER_AGENT)
        }

        val sampleApiJson = "{ \"key\": \"value\" }"

        Scenario("Client submits new APIs to new namespace") {
            val apiName = "hello" + Random().nextInt(8)
            val namespace = "test"
            Given("Api name and namespace") {}

            When("API is submitted") {
                val body = LinkedMultiValueMap<String, Any>(mapOf(
                        "version" to listOf("1.0"),
                        "url" to listOf("http://here.com"),
                        "file" to listOf(sampleApiJson.toByteArray())
                ))

                putMultipartForm("namespaces/$namespace/apis/$apiName", body)
            }

            ThenResponseIsOK()
        }

        Scenario("Client updates an existing API") {
            val apiName = "repeat-app"
            val namespace = "namespace_with_dash"

            When("Submit for first time") {
                val body1 = LinkedMultiValueMap<String, Any>(mapOf(
                        "version" to listOf("1.0"),
                        "url" to listOf("http://here.com"),
                        "file" to listOf(sampleApiJson.toByteArray())
                ))
                putMultipartForm("namespaces/$namespace/apis/$apiName", body1)
            }

            And("Submit new values for same API") {
                val body2 = LinkedMultiValueMap<String, Any>(mapOf(
                        "version" to listOf("1.0"),
                        "url" to listOf("http://new-url.com"),
                        "file" to listOf(sampleApiJson.uppercase().toByteArray())
                ))
                putMultipartForm("namespaces/$namespace/apis/$apiName", body2)
            }

            ThenResponseIsOK()
        }

        Scenario("Publish an API with authorities restriction") {
            Skip.Yes("not yet described")
        }
    }
}) {
}