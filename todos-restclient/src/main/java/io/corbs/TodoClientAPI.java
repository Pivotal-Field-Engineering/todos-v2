package io.corbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.lang.String.format;

@RestController
public class TodoClientAPI {

    private static final Logger LOG = LoggerFactory.getLogger(TodoClientAPI.class);

    private final RestTemplate restTemplate;

    @Autowired
    public TodoClientAPI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/")
    public Todo createTodo(@RequestBody Todo todo) {
        ResponseEntity<Todo> responseEntity = this.restTemplate.postForEntity("/", todo, Todo.class);
        return responseEntity.getBody();
    }

    @GetMapping("/")
    public List<Todo> retrieve() {
        ResponseEntity<List> responseEntity = this.restTemplate.getForEntity("/", List.class);
        return responseEntity.getBody();
    }

    @GetMapping("/{id}")
    public Todo retrieve(@PathVariable Integer id) {
        Todo todo;
        try {
            todo = restTemplate.getForObject("/{id}", Todo.class, id);
        } catch (HttpClientErrorException ex)   {
            if (HttpStatus.NOT_FOUND != ex.getStatusCode()) {
                throw ex;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%d", id));
        }

        return todo;
    }

    @DeleteMapping("/")
    public void delete() {
        this.restTemplate.delete("/");
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        this.restTemplate.delete("/{id}", id);
    }

    @PatchMapping("/{id}")
    public Todo update(@PathVariable Integer id, @RequestBody Todo todo) {
        Todo updated;
        try {
            updated = this.restTemplate.patchForObject("/{id}", todo, Todo.class, id);
        } catch (HttpClientErrorException ex)   {
            if (HttpStatus.NOT_FOUND != ex.getStatusCode()) {
                throw ex;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%d", id));
        }

        return updated;
    }
    
}


