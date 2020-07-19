package io.rss.openapiboard.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication(scanBasePackages = ["io.rss.openapiboard.server"])
@ConfigurationPropertiesScan(basePackages = ["io.rss.openapiboard.server"])
@EnableAsync
class OpenapiBoardServerApplication

fun main(args: Array<String>) {
	runApplication<OpenapiBoardServerApplication>(*args)
}
