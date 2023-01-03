package io.rss.openapiboard

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.rss.openapiboard.TestRequestHelpers.`from last response equals`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

object TestRequestHelpers {

    private const val SERVER = "http://localhost:8080"

    private val restTemplate = RestTemplateBuilder()
            .basicAuthentication("admin", "test")
            .defaultHeader("accepts", MediaType.APPLICATION_JSON_VALUE)
            .build()

    private val objectMapper = ObjectMapper()
    private data class CurrentResponse(val response: ResponseEntity<*>,
                                       val jsonNode: JsonNode)
    private var current: CurrentResponse? = null

    fun `GET json`(endpoint: String) {
        runForResponse {
            val headers = LinkedMultiValueMap<String, String>().apply {
                add("contentType", "application/json")
                add("accept", "application/json")
            }
            restTemplate.exchange("$SERVER/$endpoint", HttpMethod.GET, HttpEntity<Any>(headers), String::class.java)
//            restTemplate.getForEntity("$SERVER/$endpoint", String::class.java)
        }
        println(current?.response?.body)
    }

    fun `POST json`(endpoint: String, json: String) {
        runForResponse {
            restTemplate.postForEntity("$SERVER/$endpoint", HttpEntity(json, HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }), String::class.java)
        }
        println(current?.response?.body)
    }

    fun `PUT json`(endpoint: String, json: String) {
        runForResponse {
            restTemplate.exchange("$SERVER/$endpoint", HttpMethod.PUT, HttpEntity(json, HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }), String::class.java)
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

    fun <T> getFromResponse(path: String): T? {
        return if (path[0] == '$') {
            JsonPath.read(current?.response?.body, path) as? T
        } else {
            current?.jsonNode?.at(path) as? T
        }
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

    fun Any?.toJson(): String {
        return this?.let { objectMapper.writeValueAsString(it) }
                ?: ""
    }


}