package io.todos.cloudcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

@RestController
public class Api {
    private final TodosRepo todosRepo;

    @Autowired
    public Api(TodosRepo todosRepo) {
        this.todosRepo = todosRepo;
    }

    @GetMapping("/")
    public List<Todo> retrieve() {
        List<Todo> todos = new ArrayList<>();
        todosRepo.findAll().forEach(todos::add);
        return todos;
    }

    @PostMapping("/")
    public Todo create(@RequestBody Todo todo) {
        if(StringUtils.isEmpty(todo.getId())) {
            todo.setId(UUID.randomUUID().toString());
        }
        return todosRepo.save(todo);
    }

    @DeleteMapping("/")
    public void delete() {
        todosRepo.deleteAll();
    }

    @GetMapping("/{id}")
    public Todo retrieve(@PathVariable String id) {
        Todo todos = this.todosRepo.findById(id).orElse(null);
        if(todos != null) {
            return todos;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        todosRepo.deleteById(id);
    }

    @PatchMapping("/{id}")
    public Todo update(@PathVariable String id, @RequestBody Todo todo) {
        if(todo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todo can't be null");
        }
        Optional<Todo> result = this.todosRepo.findById(id);
        if(!result.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }
        Todo current = result.get();
        if(!ObjectUtils.isEmpty(todo.getComplete())) {
            current.setComplete(todo.getComplete());
        }
        if(!StringUtils.isEmpty(todo.getTitle())){
            current.setTitle(todo.getTitle());
        }
        return todosRepo.save(current);
    }
}
