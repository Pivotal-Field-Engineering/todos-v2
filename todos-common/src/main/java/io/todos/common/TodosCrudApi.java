package io.todos.common;

import java.util.List;

// todos-api
public interface TodosCrudApi {
    Todo create(Todo todo);
    List<Todo> retrieveAll();
    Todo retrieve(String id);
    Todo update(String id, Todo todo);
    void delete(String id);
    void deleteAll();
}
