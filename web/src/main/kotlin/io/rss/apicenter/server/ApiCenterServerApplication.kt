package io.rss.apicenter.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication(scanBasePackages = ["io.rss.apicenter.server"])
@ConfigurationPropertiesScan(basePackages = ["io.rss.apicenter.server"])
@EnableAsync
class ApiCenterServerApplication

fun main(args: Array<String>) {
	runApplication<ApiCenterServerApplication>(*args)
}
