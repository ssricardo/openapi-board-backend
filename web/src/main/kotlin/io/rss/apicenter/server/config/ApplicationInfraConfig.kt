package io.rss.apicenter.server.config

import io.rss.apicenter.server.helper.TokenHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
class ApplicationInfraConfig {

    @Bean("threadPoolTaskExecutor")
    fun getAsyncExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 15
        executor.maxPoolSize = 500
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setThreadNamePrefix("Async-")
        executor.initialize()
        return DelegatingSecurityContextAsyncTaskExecutor(executor)
    }

    @Bean
    fun getExecutorService(): ExecutorService {
        return Executors.newCachedThreadPool()
    }

    @Bean
    fun getTokenHelper(@Value("\${jwt.private.key}") jwtKey: String): TokenHelper {
        return TokenHelper.also {
            it.setupAlgorithm(jwtKey)
        }
    }
}