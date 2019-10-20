package io.rss.openapiboard.server.services.tmp

import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.services.AppRecordBusiness
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.PostConstruct
import javax.inject.Inject

@Profile("test")
@Service
class TestDataLoadingService {

    @Inject
    lateinit var appService: AppRecordBusiness

    private companion object {
        const val PRODUCTION = "Production"
        const val TEST = "Test"
    }

    @PostConstruct
    fun init() {
        val petStoreSource =  Files.newInputStream(Paths.get("" +
                "D:\\dev\\git\\openapi-center\\openapi-board-server\\src\\test\\resources\\test-data\\petstore-expanded.yaml"))
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

        for (i in 1 .. 5) {
            val src  = items[i]
            for (j in 1..3) {
                items.add(AppRecord(src.name, "feature/${i}_$j"))
            }
        }

        items.forEachIndexed { ind, it ->
            it.version =  if (it.namespace!!.startsWith("feature")) "1.1-SNAPSHOT" else "1.0"
            it.address = "http://localhost:808$ind/resource"
            it.source = petStoreSource

            appService.createOrUpdate(it)
        }

        println("Loading completed")
    }
}