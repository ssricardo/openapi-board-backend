package io.rss.openapiboard

import org.springframework.util.LinkedMultiValueMap

object DataProvider {

    private var json: ByteArray = javaClass.getResourceAsStream("/sample-def-petstore.yaml").readAllBytes()

    fun givenApi(apiName: String, namespace: String, version: String, requiredAuth: String? = null): String? {
        val body = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf(version),
                "url" to listOf("http://some-server.local.com"),
                "file" to listOf(json),
                "requiredAuthority" to listOf(requiredAuth)
        ))

        RestRequestHelper.putMultipartForm("namespaces/$namespace/apis/$apiName", body)
        return RestRequestHelper.getFromResponse<String>("$")?.removeSurrounding("\"")
    }

    fun givenNamespace(name: String, requiredAuths: List<String> = emptyList()) {
        RestRequestHelper.postJsonObject("namespaces", mapOf("name" to name, "authorities" to requiredAuths))
        RestRequestHelper.assertResponseIsOK()
    }
}