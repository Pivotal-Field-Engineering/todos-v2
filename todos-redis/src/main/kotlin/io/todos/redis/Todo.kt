package io.todos.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("todos")
class Todo {
    @Id
    var id: String? = null
    var title: String? = null
    var complete: Boolean = false
    fun complete() {
        this.complete = true
    }

    fun incomplete() {
        this.complete = false
    }
}