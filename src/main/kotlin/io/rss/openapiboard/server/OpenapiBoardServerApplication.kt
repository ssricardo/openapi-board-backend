package io.rss.openapiboard.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan


@SpringBootApplication
@ComponentScan(basePackages = ["io.rss.openapiboard.server"])
//@EnableWebSecurity()
class OpenapiBoardServerApplication

fun main(args: Array<String>) {
	runApplication<OpenapiBoardServerApplication>(*args)
}
