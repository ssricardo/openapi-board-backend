package sandbox

import com.fasterxml.jackson.annotation.JsonAutoDetect
import io.rss.openapiboard.server.persistence.MethodType
import io.rss.openapiboard.server.persistence.entities.request.ParameterType
import io.rss.openapiboard.server.services.to.ParameterMemoryTO
import io.rss.openapiboard.server.services.to.RequestMemoryRequestResponse
import io.rss.openapiboard.server.services.to.SubscriptionRequestResponse
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils.createAuthorityList
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import javax.annotation.PostConstruct
import javax.ws.rs.core.Response
import kotlin.random.Random


/** Populates the DB with sample data (for testing, demonstration). */
@Service
class SandboxDataLoadingService {

    private val serverBase = "http://localhost:8080"
    private val restTemplate: RestTemplate = RestTemplateBuilder()
            .basicAuthentication("admin", "test")
            .build()

    val randomizer = Random(30)

    private companion object {
        const val PRODUCTION = "Production"
        const val TEST = "Test"
        const val FEATURE = "feature-1_1"
    }

    @PostConstruct
    fun init() {
        try {
            print("========================== Trying to create test data ============================")

            val petStoreSource = javaClass.getResourceAsStream(
                    "/sample-def-petstore.yaml")
                    .use {
                        it.bufferedReader().readText()
                    }

            val authorities: Collection<GrantedAuthority> = createAuthorityList("MANAGER")
            val authentication: Authentication = UsernamePasswordAuthenticationToken(
                    "admin", "MANAGER", authorities)
            SecurityContextHolder.getContext().authentication = authentication

            val apiRecordList = createApiRecords(petStoreSource)

            createExampleMemory(apiRecordList)
            createSubscriptions()

            println("Loading completed")
        } catch (e: Exception) {
            e.printStackTrace()
            println("""Error on loading fake data. Ignoring...
                |${e.message}
            """.trimMargin())
        }
    }

    private fun createApiRecords(petStoreSource: String): MutableList<ApiRecordDto> {
        val items = mutableListOf(
                ApiRecordDto("Orders", PRODUCTION),
                ApiRecordDto("Orders", TEST),
                ApiRecordDto("Products", PRODUCTION),
                ApiRecordDto("Products", TEST),
                ApiRecordDto("People", PRODUCTION),
                ApiRecordDto("People", TEST),
                ApiRecordDto("Disks", PRODUCTION),
                ApiRecordDto("Songs", PRODUCTION),
                ApiRecordDto("Disks", FEATURE),
                ApiRecordDto("Songs", FEATURE)
        )

        for (i in 1..5) {
            val src = items[i]
            for (j in 1..3) {
                items.add(ApiRecordDto(src.name, "${i}_$j"))
            }
        }

        items.forEachIndexed { ind, api ->
            api.body.version = if (api.namespace.startsWith("feature")) "1.1-SNAPSHOT" else "1.0"
            api.body.url = "http://localhost:808$ind/resource"
            api.body.file = getRandomChangedSource(petStoreSource).byteInputStream()

            val map: MultiValueMap<String, Any> = LinkedMultiValueMap()
            with(map) {
                add("version", api.body.version)
                add("url", api.body.url)
                add("file", api.body.file?.readBytes())
            }

            restTemplate
                    .put("$serverBase/namespaces/${api.namespace}/apis/${api.name}",
                            HttpEntity(map, HttpHeaders().apply {
                                contentType = MediaType.MULTIPART_FORM_DATA
                            }))
        }

        return items
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private data class ApiRecordDto(val name: String, val namespace: String, var body: ApiRecordBody = ApiRecordBody())

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private data class ApiRecordBody(var version: String? = null, var url: String? = null, var file: InputStream? = null)

    private fun createSubscriptions() {
        for (i in 0..10) {
            val subs = SubscriptionRequestResponse().apply {
                apiName = "Products"
                email = "ricardo.test$i@testMail.com"
            }

            restTemplate
                .postForEntity("$serverBase/subscriptions",
                        HttpEntity(subs, HttpHeaders().apply {
                            contentType = MediaType.APPLICATION_JSON
                        }), Response::class.java)

        }
        println("Subscriptions created")
    }

    private fun createExampleMemory(items: MutableList<ApiRecordDto>) {
        items.forEach {
            for (i in 0..3) {
                try {
                    val memory = RequestMemoryRequestResponse(null, it.namespace, it.name,
                            "/pets", if (i % 2 == 0) MethodType.GET else MethodType.POST).apply {
                        title = if (i % 2 == 0) "Test resource for bla" else "Special request"
                        body = "{'val': 'Any silly sample'}"
                        requestHeaders.addAll(arrayOf(ParameterMemoryTO(null, ParameterType.HEADER, "type", "yaml")))
                        parameters.addAll(arrayOf(ParameterMemoryTO(null, ParameterType.QUERY, "city", "Sao Paulo")))
                    }

                    restTemplate
                        .postForEntity("${serverBase}/requests",
                                HttpEntity(memory, HttpHeaders().apply {
                                    contentType = MediaType.APPLICATION_JSON
                                }), Response::class.java)
                } catch (e: RestClientResponseException) {
                    if (e.rawStatusCode != 409) {
                        throw e
                    }
                    // ignore: randomized data didn't match an operation
                }
            }
        }

    }

    private fun getRandomChangedSource(petStoreSource: String): String {
        var result = petStoreSource
        if (randomizer.nextBoolean()) {
            result = result.replace("200", "300")
        }
        if (randomizer.nextBoolean()) {
            result = result.replace("NewPet", "Dino")
        }
        if (randomizer.nextBoolean()) {
            result = result.replace("/pets", "/books")
        }
        if (randomizer.nextBoolean()) {
            result = result.replace("deletes a single pet based on the ID supplied", "deletes whatever it wants")
        }
        if (randomizer.nextBoolean()) {
            result = result.replace("unexpected error", "Oh NOOO!")
        }
        return result
    }
}