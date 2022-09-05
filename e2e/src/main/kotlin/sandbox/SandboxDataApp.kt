package sandbox

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

fun main(args: Array<String>) {
    with(AnnotationConfigApplicationContext(SandboxDataApp::class.java)) {
        val loadingService = getBean(SandboxDataLoadingService::class.java)
        loadingService.init()
    }
}

@Configuration
@ComponentScan(basePackages = ["sandbox"])
class SandboxDataApp