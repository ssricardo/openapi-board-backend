package io.rss.openapiboard

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

object RestRequestHelper {

    private const val SERVER = "http://localhost:8080"

    val USER_AGENT = "agent" to "test00"
    val USER_ADMIN = "admin" to "test00"
    val USER_BLUE = "blue" to "accessBlueOnly"
    val USER_GREEN = "green" to "accessGreenOnly"

    private var restTemplateBuilder = RestTemplateBuilder()
            .defaultHeader("accepts", MediaType.APPLICATION_JSON_VALUE)


    private val restTemplate: RestTemplate
        get() {
            return restTemplateBuilder.build()
        }

    private val objectMapper = ObjectMapper()
    private data class CurrentResponse(val response: ResponseEntity<*>,
                                       val jsonNode: JsonNode)
    private var current: CurrentResponse? = null

    fun givenBasicAuth(userPass: Pair<String, String>) {
        restTemplateBuilder = restTemplateBuilder.basicAuthentication(userPass.first, userPass.second)
    }

    fun getJson(endpoint: String) {
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
            val endpointNoSlash = endpoint.removePrefix("/")
            restTemplate.postForEntity("$SERVER/$endpointNoSlash", HttpEntity(json, HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }), String::class.java)
        }
        println(current?.response?.body)
    }

    fun postJson(endpoint: String, json: String) = `POST json`(endpoint, json)

    fun postJsonObject(endpoint: String, data: Any) = postJson(endpoint, data.toJson())

    fun putJson(endpoint: String, json: String) {
        runForResponse {
            restTemplate.exchange("$SERVER/$endpoint", HttpMethod.PUT, HttpEntity(json, HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }), String::class.java)
        }
    }

    fun putJson(endpoint: String, data: Any) = putJson(endpoint, data.toJson())

    fun `PUT multipart-form`(endpoint: String, payload: MultiValueMap<String, Any>) {
        runForResponse {
            val result = restTemplate.exchange("$SERVER/$endpoint", HttpMethod.PUT, HttpEntity(payload, HttpHeaders().apply {
                contentType = MediaType.MULTIPART_FORM_DATA
                accept = listOf(MediaType.APPLICATION_JSON)
            }), String::class.java)
            result
        }
    }

    fun putMultipartForm(endpoint: String, payload: MultiValueMap<String, Any>) = `PUT multipart-form`(endpoint, payload)

    fun `assert response is`(expectedStatus: HttpStatus) {
        checkNotNull(current) { "No response is present. Verify that a request was done beforehand" }
        assertEquals(expectedStatus, current?.response?.statusCode)
    }

    fun assertResponseIs(expectedStatus: HttpStatus) = `assert response is`(expectedStatus)

    fun `assert response is OK`() {
        `assert response is`(HttpStatus.OK)
    }

    fun <T> getFromResponse(path: String): T? {
        return if (path[0] == '$') {
            JsonPath.read(current?.response?.body as? String, path) as? T
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

    infix fun String.fromLastResponseEquals(value: Any) = this.`from last response equals`(value)

    infix fun String.asListFromLastResponseContains(value: Any) {
        val list: List<Any> = JsonPath.read(current?.response?.body as? String, this)
        assertTrue(value in list)
    }

    infix fun String.asListFromLastResponseNotInclude(value: Any) {
        val list: List<Any> = JsonPath.read(current?.response?.body as? String, this)
        assertTrue(value !in list)
    }

    private fun runForResponse(fn: () -> ResponseEntity<*>) {
        current = null
        try {
            val res = fn()
            val body: String = (res.body as? String) ?: "{}"
            current = CurrentResponse(res, objectMapper.readTree(body))
        } catch (e: HttpClientErrorException) {
            current = CurrentResponse(ResponseEntity.status(e.statusCode).body(e.responseBodyAsString),
                    objectMapper.readTree(""))
        }
    }

    fun Any?.toJson(): String {
        return this?.let { objectMapper.writeValueAsString(it) }
                ?: ""
    }

    fun deleteHttp(endpoint: String) {
        runForResponse {
            val result = restTemplate.exchange("${SERVER}/$endpoint", HttpMethod.DELETE,
                    HttpEntity<String>(LinkedMultiValueMap(mapOf(
                            "accept" to listOf("application/json")
                    )))
                    , String::class.java)
            result
        }
    }

    fun assertResponseIsOK() = `assert response is OK`()
}