package io.rss.openapiboard

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.util.MultiValueMap

object TestRequestHelpers {

    private const val SERVER = "http://localhost:8080"

    private val restTemplate = RestTemplateBuilder()
            .basicAuthentication("admin", "test")
            .build()

    private val objectMapper = ObjectMapper()
    private data class CurrentResponse(val response: ResponseEntity<*>,
                                       val jsonNode: JsonNode)
    private var current: CurrentResponse? = null

    fun `GET json`(endpoint: String) {
        runForResponse {
            restTemplate.getForEntity("$SERVER/$endpoint", String::class.java)
        }
        println(current?.response?.body)
//        current = null
//        val res = restTemplate.getForEntity(endpoint, String::class.java)
//        current = CurrentResponse(res, objectMapper.readTree(res.body as String))
    }

    fun putJson(endpoint: String, json: String) {
        runForResponse {
            restTemplate.getForEntity(endpoint, HashMap::class.java)
        }
    }

    fun `PUT multipart-form`(endpoint: String, payload: MultiValueMap<String, Any>) {
        runForResponse {
            val result = restTemplate.exchange("$SERVER/$endpoint", HttpMethod.PUT, HttpEntity(payload, HttpHeaders().apply {
                contentType = MediaType.MULTIPART_FORM_DATA
            }), String::class.java)
            result
        }
    }

    fun `assert response is`(expectedStatus: HttpStatus) {
        checkNotNull(current) { "No response is present. Verify that a request was done beforehand" }
        assertEquals(current?.response?.statusCode, expectedStatus)
    }

    fun `assert response is OK`() {
        `assert response is`(HttpStatus.OK)
    }

    infix fun String.`from last response equals`(value: Any) {
        if (this[0] == '$') {
            assertEquals(value, JsonPath.read(current?.response?.body as? String, this))
        } else {
            assertEquals(value, current?.jsonNode?.at(this))
        }
    }

    infix fun String.`as list from last response contains`(value: Any) {
        val list: List<Any> = JsonPath.read(current?.response?.body as? String, this)
        assertTrue(value in list)
    }

    private fun runForResponse(fn: () -> ResponseEntity<*>) {
        current = null
        val res = fn()
        val body: String = (res.body as? String) ?: "{}"
        current = CurrentResponse(res, objectMapper.readTree(body))
    }

}