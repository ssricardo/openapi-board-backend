package io.rss.openapiboard.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor

@Configuration
class GeneralConfig {

    @Bean("threadPoolTaskExecutor")
    fun getAsyncExecutor(): TaskExecutor? {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 15
        executor.maxPoolSize = 500
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setThreadNamePrefix("Async-")
        executor.initialize()
        return DelegatingSecurityContextAsyncTaskExecutor(executor)
    }
}