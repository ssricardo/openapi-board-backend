package io.rss.openapiboard.server.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import javax.inject.Inject


/** Sets up a very BASIC configuration for security.
 * For other cases, a custom configuration must be set.
 *  */

@Configuration
@EnableWebSecurity(debug = true)
//@EnableGlobalMethodSecurity(jsr250Enabled = false, securedEnabled = false, prePostEnabled = true)
class SecurityConfig: WebSecurityConfigurerAdapter() {

    @Inject
    private lateinit var config: UserConfig

    @Throws(Exception::class)
    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/**")
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        var aConfig = auth.inMemoryAuthentication()
        config.entries.forEach {
            aConfig.withUser(it.name)
                    .password(encoder.encode(it.password))
                    .authorities(*it.roles.toTypedArray())
        }
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
    }

}