package io.rss.apicenter.server.security.config

import io.rss.apicenter.server.security.AuthEntryPointJwt
import io.rss.apicenter.server.security.AuthTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.ldap.core.ContextSource
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.userdetails.PersonContextMapper
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.annotation.Resource


/**
 * Sets up some basic configuration for security.
 * For other cases, a custom configuration must be set.
 *  */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig {

    @Resource
    private lateinit var config: UserConfig

    @Resource
    private lateinit var authEntryPoint: AuthEntryPointJwt

    @Resource
    @Lazy
    private lateinit var authManager: AuthenticationManager

    @Bean
    @Profile("!ldap")
    fun userDetailsService(): InMemoryUserDetailsManager {
        val userDetails = config.entries.map {
            User.withDefaultPasswordEncoder()
                    .username(it.name)
                    .password(it.password)
                    .authorities(*it.roles.toTypedArray())
                    .build()
        }

        return InMemoryUserDetailsManager(userDetails)
    }

    @Bean
    fun authManager(http: HttpSecurity, userDetailService: UserDetailsService): AuthenticationManager {
        return http.getSharedObject(AuthenticationManagerBuilder::class.java)
                .userDetailsService(userDetailService)
                .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder())
                .and()
                .build()
    }

    @Bean
    fun configureCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring()
                    .antMatchers("/test", "/test/*",
                            "/auth/", "/auth/*",
                            "/m/*" // Input from mail (with token)
                    )
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
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

        return http.build()
    }

    /* Ldap config for some reference but not yet working well - Also needs to have the configs externalized */
    @Bean
    @Profile("ldap")
    fun contextSource(): ContextSource {
        return DefaultSpringSecurityContextSource("ldap://localhost:389/dc=example,dc=org").apply {
            this.userDn = "cn=admin,dc=example,dc=org"
            this.password = "admin00"
        }
    }

    @Bean
    @Profile("ldap")
    fun ldapAuthenticationManager(
            contextSource: BaseLdapPathContextSource): AuthenticationManager {
        val factory = LdapBindAuthenticationManagerFactory(contextSource).apply {
            setUserSearchFilter("(uid={0})")
            setUserSearchBase("ou=users")
            setUserDetailsContextMapper(PersonContextMapper())
        }
        return factory.createAuthenticationManager()
    }
}