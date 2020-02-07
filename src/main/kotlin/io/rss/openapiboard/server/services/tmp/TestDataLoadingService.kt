package io.rss.openapiboard.server.services.tmp

import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.services.AppRecordBusiness
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct
import javax.inject.Inject
import kotlin.random.Random

@Profile("test")
@Service
class TestDataLoadingService {

    @Inject
    lateinit var appService: AppRecordBusiness

    val randomizer = Random(30)

    private companion object {
        const val PRODUCTION = "Production"
        const val TEST = "Test"
    }

    @PostConstruct
    fun init() {
        try {
            print("========================== Trying to create test data ============================")

            val petStoreSource =  Files.newInputStream(Paths.get(
                    "/home/ricardo/projects/ricardo/openapi-board/openapi-board/samples/petstore-expanded.yaml"))
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

            for (i in 1 .. 10) {
                val src  = items[i]
                for (j in 1..3) {
                    items.add(AppRecord(src.name, "feature/${i}_$j"))
                }
            }

            items.forEachIndexed { ind, it ->
                it.version =  if (it.namespace!!.startsWith("feature")) "1.1-SNAPSHOT" else "1.0"
                it.address = "http://localhost:808$ind/resource"
                it.source = getRandomChangedSource(petStoreSource)

                appService.createOrUpdate(it)
            }

            println("Loading completed")
        } catch (e: Exception) {
            println("""Error on loading fake data. Ignoring...
                |${e.message}
            """.trimMargin())
        }
    }

    private fun getRandomChangedSource(petStoreSource: String): String {
        var result = petStoreSource
        if (randomizer.nextBoolean()) {
            result = result.replace("200", "300")
        }
        if (randomizer.nextBoolean()) {
            result = result.replace("name", "alias")
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