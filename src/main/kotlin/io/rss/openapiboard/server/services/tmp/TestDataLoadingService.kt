package io.rss.openapiboard.server.services.tmp

import io.rss.openapiboard.server.persistence.AppOperationType
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.request.ParameterKind
import io.rss.openapiboard.server.services.AppRecordHandler
import io.rss.openapiboard.server.services.RequestMemoryHandler
import io.rss.openapiboard.server.services.to.ParameterMemoryTO
import io.rss.openapiboard.server.services.to.RequestMemoryViewTO
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils.createAuthorityList
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.transaction.Transactional
import kotlin.random.Random


@Profile("test")
@Service
class TestDataLoadingService {

    @Inject
    lateinit var appService: AppRecordHandler

    @Inject
    lateinit var requestMemoryHandler: RequestMemoryHandler

    val randomizer = Random(30)

    private companion object {
        const val PRODUCTION = "Production"
        const val TEST = "Test"
    }

    @PostConstruct
    fun init() {
        try {
            print("========================== Trying to create test data ============================")

            val petStoreSource = Files.newInputStream(Paths.get(
                    "/home/ricardo/projects/ricardo/openapi-board/openapi-board-backend/src/test/resources/test-data/petstore-expanded.yaml"))
                    .use {
                        it.bufferedReader().readText()
                    } // FIXME
            val items = mutableListOf(AppRecord("Orders", PRODUCTION),
                    AppRecord("Orders", TEST),
                    AppRecord("Products", PRODUCTION),
                    AppRecord("Products", TEST),
                    AppRecord("People", PRODUCTION),
                    AppRecord("People", TEST),
                    AppRecord("Disks", PRODUCTION),
                    AppRecord("Songs", PRODUCTION)
                    )

            for (i in 1..7) {
                val src  = items[i]
                for (j in 1..3) {
                    items.add(AppRecord(src.name, "feature/${i}_$j"))
                }
            }

            val authorities: Collection<GrantedAuthority> = createAuthorityList("MANAGER")
            val authentication: Authentication = UsernamePasswordAuthenticationToken(
                    "admin", "MANAGER", authorities)
            SecurityContextHolder.getContext().authentication = authentication

            items.forEachIndexed { ind, it ->
                it.version =  if (it.namespace!!.startsWith("feature")) "1.1-SNAPSHOT" else "1.0"
                it.address = "http://localhost:808$ind/resource"
                it.source = getRandomChangedSource(petStoreSource)
                it.source = petStoreSource

                appService.createOrUpdate(it)
            }

            createExampleMemory(items)

            println("Loading completed")
        } catch (e: Exception) {
            println("""Error on loading fake data. Ignoring...
                |${e.message}
            """.trimMargin())
        }
    }

    private fun createExampleMemory(items: MutableList<AppRecord>) {
        items.forEach {
            for (i in 0..3) {
                try {
                    requestMemoryHandler.saveRequest(RequestMemoryViewTO(null, it.namespace, it.name,
                            "/pets", if (i % 2 == 0) AppOperationType.GET else AppOperationType.POST).apply {
                        title = if (i % 2 == 0) "Test resource for bla" else "Special request"
                        body = "{'val': 'Any silly sample'}"
                        requestHeaders.addAll(arrayOf(ParameterMemoryTO(null, ParameterKind.HEADER, "type", "yaml")))
                        parameters.addAll(arrayOf(ParameterMemoryTO(null, ParameterKind.QUERY, "city", "Sao Paulo")))
                    })
                } catch (e: Exception) {
                    println(e)
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
        return result
    }
}