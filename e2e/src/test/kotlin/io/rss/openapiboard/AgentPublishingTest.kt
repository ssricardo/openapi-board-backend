package io.rss.openapiboard

import io.rss.openapiboard.TestRequestHelpers.`assert response is OK`
import io.rss.openapiboard.TestRequestHelpers.`PUT multipart-form`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap
import java.util.*

@DisplayName("How agent apps interact with this system")
class AgentPublishingTest {

    @Test
    fun `It should submit new APIs to new namespace`() {
        val apiName = "hello" + Random().nextInt(8)
        val namespace = "test"
        val body = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf("1.0"),
                "url" to listOf("http://here.com"),
                "file" to listOf(sampleApiJson().toByteArray())
        ))

        `PUT multipart-form`("namespaces/$namespace/apis/$apiName", body)
        `assert response is OK`()
    }

    @Test
    fun `It should submit new updates to existing APIs`() {
        val apiName = "repeat-app"
        val namespace = "namespace.with-dash"
        val body1 = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf("1.0"),
                "url" to listOf("http://here.com"),
                "file" to listOf(sampleApiJson().toByteArray())
        ))
        `PUT multipart-form`("namespaces/$namespace/apis/$apiName", body1)

        val body2 = LinkedMultiValueMap<String, Any>(mapOf(
                "version" to listOf("1.0"),
                "url" to listOf("http://new-url.com"),
                "file" to listOf(sampleApiJson().uppercase().toByteArray())
        ))
        `PUT multipart-form`("namespaces/$namespace/apis/$apiName", body2)
        `assert response is OK`()
    }

    private fun sampleApiJson() = "{ \"key\": \"value\" }"
}