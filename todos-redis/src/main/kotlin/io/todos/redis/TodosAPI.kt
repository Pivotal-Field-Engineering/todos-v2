package io.todos.redis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.util.ObjectUtils
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class TodosAPI(@Autowired @Qualifier("todosRepo") val repo: TodosRepo,
    @Value("\${todos.api.limit}") val limit: Int) {

    @GetMapping("/")
    fun retrieve(): List<Todo> {
        return this.repo.findAll().toList()
    }

    @ResponseStatus(CREATED)
    @PostMapping("/")
    fun create(@RequestBody todo: Todo): Todo {
        throwIfOverLimit()
        val createObject = Todo()
        if(ObjectUtils.isEmpty(todo.id)) {
            createObject.id = UUID.randomUUID().toString()
        } else {
            createObject.id = todo.id
        }
        if(!ObjectUtils.isEmpty(todo.title)) {
            createObject.title = todo.title
        }
        if(!ObjectUtils.isEmpty(todo.complete)) {
            createObject.complete = todo.complete
        }
        return this.repo.save(createObject)
    }

    @ResponseStatus(OK)
    @PostMapping("/{id}")
    fun put(@PathVariable id: String, @RequestBody todo: Todo): Todo {
        if(todo.id == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.id cannot be null on put")
        }
        if(todo.id != id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "todos.id ${todo.id} and id $id are inconsistent")
        }
        return this.repo.save(todo)
    }

    @PostMapping("/load/{size}")
    fun load(@PathVariable("size") size: Int = 1000) {
        for(i in 1..size) {
            throwIfOverLimit()
            val todo = Todo()
            todo.id = UUID.randomUUID().toString()
            todo.title = "make bacon pancakes $i"
            todo.complete = false
            this.repo.save(todo)
        }
    }

    @DeleteMapping("/")
    fun deleteAll() {
        this.repo.deleteAll()
    }

    @GetMapping("/{id}")
    fun retrieve(@PathVariable("id") id: String): Todo {
        return this.repo.findById(id).orElseThrow {
            throw ResponseStatusException(NOT_FOUND, "todo.id=${id}")
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String){
        this.repo.deleteById(id)
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable("id") id: String, @RequestBody todo: Todo): Todo {
        val existing = retrieve(id)
        if (!ObjectUtils.isEmpty(todo.complete)) {
            existing.complete = todo.complete
        }
        if (!StringUtils.isEmpty(todo.title)) {
            existing.title = todo.title
        }
        return this.repo.save(existing)
    }

    @GetMapping("/find")
    fun findByTitle(@RequestParam("title") title: String): List<Todo> {
        return this.repo.findByTitle(title)
    }

    @GetMapping("/limit")
    fun limit(): Limit {
        return Limit(this.repo.count().toInt(), this.limit)
    }

    private fun throwIfOverLimit() {
        val count = this.repo.count()
        if(count >= limit) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "todos.api.limit=$limit, todos.size=$count")
        }
    }
}
