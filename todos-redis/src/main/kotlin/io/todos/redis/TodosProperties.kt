package io.todos.redis

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("todos")
class TodosProperties {

    val api = Api()

    val ids = Ids()

    class Api {
        var limit: Int = 1024
    }

    class Ids {
        var tinyId: Boolean = true
    }
}