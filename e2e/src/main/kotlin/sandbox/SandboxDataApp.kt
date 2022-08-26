package sandbox

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

fun main(args: Array<String>) {
//    runApplication<SandboxDataApp>(*args)
    with(AnnotationConfigApplicationContext(SandboxDataApp::class.java)) {
        val loadingService = getBean(SandboxDataLoadingService::class.java)
        loadingService.init()
    }
}

//@SpringBootApplication(scanBasePackages = [
////    "sandbox", "io.rss.openapiboard.server.services"
//])
//@EnableJpaRepositories("io.rss.openapiboard.server.persistence")
@Configuration
@ComponentScan(basePackages = ["sandbox"])
class SandboxDataApp