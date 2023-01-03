package io.rss.openapiboard

import io.rss.openapiboard.TestRequestHelpers.`GET json`
import io.rss.openapiboard.TestRequestHelpers.`POST json`
import io.rss.openapiboard.TestRequestHelpers.`PUT multipart-form`
import io.rss.openapiboard.TestRequestHelpers.`as list from last response contains`
import io.rss.openapiboard.TestRequestHelpers.`assert response is OK`
import io.rss.openapiboard.TestRequestHelpers.`from last response equals`
import io.rss.openapiboard.TestRequestHelpers.getFromResponse
import org.junit.jupiter.api.*
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
    private lateinit var testApiId: Map<String, String>

    @BeforeAll
    fun beforeAll() {

        println("Loading sample data")
        givenNamespace("brasil", listOf())
        givenNamespace("america", listOf())
        givenNamespace("europe", listOf())
        givenNamespace("asia", listOf())

//        println(`GET json`("ns/test"))

        testApiId = testData.associate { (name, ns, version) ->
            val id = givenApi(name, ns, version)
            name to id!!
        }
    }

    @Test
    fun `it should list namespaces`() {
        `GET json`("namespaces")
        "$[*]" `as list from last response contains` "brasil"
    }

    @Test
    fun `it should list apis in a given namespace`() {
        `GET json`("apis?nm=brasil")
        "$.*.name" `as list from last response contains` "ocean"
    }

    @Test
    fun `it should get the current version of a given api`() {
        `GET json`("apis/" + testApiId["skyApp"]!!)
        `assert response is OK`()
        "$.version" `from last response equals` "1.2"
    }

    @Test
    fun `it should not view a namespace from distinct authority`() {
        TODO()
    }

    @Test
    fun `it should not view an API from distinct authority`() {
        TODO()
    }

    @Test
    @Disabled
    fun `it should get the openApi definition a given api`() {
        `GET json`("namespaces/brasil/apis/skyApp/source")
        "$" `from last response equals` anyJson
    }

    private fun givenApi(apiName: String, namespace: String, version: String): String? {
        val body = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf(version),
                "url" to listOf("http://here.com"),
                "file" to listOf(anyJson.toByteArray())
        ))

        `PUT multipart-form`("namespaces/$namespace/apis/$apiName", body)
        return getFromResponse<String>("$")?.removeSurrounding("\"")
    }

    private fun givenNamespace(name: String, requiredAuths: List<String>) {
        `POST json`("namespaces", """
            {
                "name": "$name"
            }
        """)
    }
}