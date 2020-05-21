package io.rss.openapiboard.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication(scanBasePackages = ["io.rss.openapiboard.server"])
@EnableConfigurationProperties
@EnableAsync
class OpenapiBoardServerApplication

fun main(args: Array<String>) {
	runApplication<OpenapiBoardServerApplication>(*args)
}
