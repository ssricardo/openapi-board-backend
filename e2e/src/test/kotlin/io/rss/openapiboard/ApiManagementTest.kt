package io.rss.openapiboard

import io.rss.openapiboard.TestRequestHelpers.`GET json`
import io.rss.openapiboard.TestRequestHelpers.`PUT multipart-form`
import io.rss.openapiboard.TestRequestHelpers.`as list from last response contains`
import io.rss.openapiboard.TestRequestHelpers.`from last response equals`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.util.LinkedMultiValueMap

@DisplayName("Manager app can search APIs, namespaces")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiManagementTest {

    private val anyJson = """ {"content": "doesnt-matter"} """

    private val testData = listOf(
            listOf("hello", "america", "1.0"),
            listOf("bye", "europe", "1.0-SNAPSHOT"),
            listOf("skyApp", "brasil", "1.1"),
            listOf("skyApp", "brasil", "1.2"),
            listOf("ocean", "brasil", "2.0"),
            listOf("bigData", "asia", "1"),
    )

    @BeforeAll
    fun beforeAll() {
        println("Loading sample data")
        testData.forEach { (name, ns, version) ->
            givenApi(name, ns, version)
        }
    }

    @Test
    fun `it should list namespaces`() {
        `GET json`("namespaces")
        "$[*]" `as list from last response contains` "brasil"
    }

    @Test
    fun `it should list apis in a given namespace`() {
        `GET json`("namespaces/brasil")
        "$.*.name" `as list from last response contains` "ocean"
    }

    @Test
    fun `it should get the current version of a given api`() {
        `GET json`("namespaces/brasil/apis/skyApp")
        "$.version" `from last response equals` "1.2"
    }

    @Test
    @Disabled
    fun `it should get the openApi definition a given api`() {
        `GET json`("namespaces/brasil/apis/skyApp/source")
        "$" `from last response equals` anyJson
    }

    private fun givenApi(apiName: String, namespace: String, version: String) {
        val body = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf(version),
                "url" to listOf("http://here.com"),
                "file" to listOf(anyJson.toByteArray())
        ))

        `PUT multipart-form`("namespaces/$namespace/apis/$apiName", body)
    }
}