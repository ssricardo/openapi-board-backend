package io.rss.openapiboard

import org.springframework.util.LinkedMultiValueMap

object DataProvider {

    private var json: ByteArray = javaClass.getResourceAsStream("/sample-def-petstore.yaml").readAllBytes()

    fun givenApi(apiName: String, namespace: String, version: String) {
        val body = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf(version),
                "url" to listOf("http://some-server.local.com"),
                "file" to listOf(json)
        ))

        TestRequestHelpers.`PUT multipart-form`("namespaces/$namespace/apis/$apiName", body)
    }
}