package io.todos.data

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository("todosRepo")
interface TodosRepo : PagingAndSortingRepository<Todo, String>