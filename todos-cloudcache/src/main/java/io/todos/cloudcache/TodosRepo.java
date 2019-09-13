package io.todos.cloudcache;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodosRepo extends CrudRepository<Todo, String> {
}
