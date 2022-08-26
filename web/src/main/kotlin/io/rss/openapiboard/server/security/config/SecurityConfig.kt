package io.rss.openapiboard.server.security.config

import io.rss.openapiboard.server.security.AuthEntryPointJwt
import io.rss.openapiboard.server.security.AuthTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.annotation.Resource


/** Sets up a very BASIC configuration for security.
 * For other cases, a custom configuration must be set.
 *  */

@Configuration
@EnableWebSecurity()
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig: WebSecurityConfigurerAdapter() {

    @Resource
    private lateinit var config: UserConfig

    @Resource
    private lateinit var authEntryPoint: AuthEntryPointJwt

    @Resource
    @Lazy
    private lateinit var authManager: AuthenticationManager

    override fun configure(auth: AuthenticationManagerBuilder) {
        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        var aConfig = auth.inMemoryAuthentication()
        config.entries.forEach {
            aConfig.withUser(it.name)
                    .password(encoder.encode(it.password))
                    .authorities(*it.roles.toTypedArray())
        }
    }

    @Bean
    override fun authenticationManager(): AuthenticationManager {
        return super.authenticationManager()
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/test","/test/*", "/auth/", "/auth/*",
                "/m/*" // Input from mail (with token)
        )
    }

    override fun configure(http: HttpSecurity) {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .exceptionHandling().authenticationEntryPoint(authEntryPoint)
        .and()
            .authorizeRequests()
                .anyRequest()
                .authenticated()
        .and()
            .csrf().disable()

        http.addFilterBefore(AuthTokenFilter(authManager), UsernamePasswordAuthenticationFilter::class.java)
    }
}