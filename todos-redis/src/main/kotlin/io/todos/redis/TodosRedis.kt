package io.todos.redis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@EnableRedisRepositories
@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient
class TodosRedis

fun main(args: Array<String>) {
    runApplication<TodosRedis>(*args)
}

