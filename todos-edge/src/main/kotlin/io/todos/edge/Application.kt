package io.todos.edge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableDiscoveryClient
@SpringBootApplication
@RefreshScope
@Configuration
@EnableWebFluxSecurity
class Application {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange().anyExchange().permitAll().and().httpBasic().disable().csrf().disable().build()
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
