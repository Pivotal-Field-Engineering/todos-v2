package io.todos.data

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository("todosRepo")
interface TodosRepo : CrudRepository<Todo, String>