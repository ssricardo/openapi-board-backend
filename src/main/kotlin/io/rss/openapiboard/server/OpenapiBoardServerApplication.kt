package io.rss.openapiboard.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication
@ComponentScan(basePackages = ["io.rss.openapiboard.server"])
//@EnableWebSecurity()
@EnableAsync
class OpenapiBoardServerApplication

fun main(args: Array<String>) {
	runApplication<OpenapiBoardServerApplication>(*args)
}
