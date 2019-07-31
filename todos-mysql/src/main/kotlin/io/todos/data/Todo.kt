package io.todos.data

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="todos")
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