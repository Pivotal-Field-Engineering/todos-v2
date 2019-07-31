package io.todos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@RestController
public class TodosAPI {

    private static final Logger LOG = LoggerFactory.getLogger(TodosAPI.class);

    private final Map<String, Todo> todos = Collections.synchronizedMap(new LinkedHashMap<>());

    private final TodosProperties properties;

    @Autowired
    public TodosAPI(TodosProperties properties) {
        LOG.info("TodosAPI booting with todos.api.limit=" + properties.getApi().getLimit());
        this.properties = properties;
    }

    @PostMapping("/")
    public Mono<Todo> create(@RequestBody Mono<Todo> todo) {
        if(todos.size() < this.properties.getApi().getLimit()) {
            return todo.map(it -> {
                if(properties.getIds().getTinyId()) {
                    it.setId(UUID.randomUUID().toString().substring(0, 8));
                } else {
                    it.setId(UUID.randomUUID().toString());
                }
                todos.put(it.getId(), it);
                return todos.get(it.getId());
            });
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                format("todos.api.limit=%d, todos.size()=%d", this.properties.getApi().getLimit(), todos.size()));
        }
    }

    @GetMapping("/")
    public Flux<Todo> retrieve() {
        return Flux.fromIterable(todos.values());
    }

    @GetMapping("/completed")
    public Flux<Todo> completed(@RequestParam(defaultValue = "0") Integer n) {
        return Flux.fromIterable(todos.values())
            .filter(Todo::getCompleted)
                .delayElements(Duration.ofMillis(n));
    }

    @GetMapping("/tag/{tag}")
    public Flux<Todo> tags(@PathVariable String tag) {
        return Flux.fromIterable(todos.values())
            .filter(todo -> todo.getTitle().contains(tag));
    }

    @GetMapping("/{id}")
    public Mono<Todo> retrieve(@PathVariable String id) {
        if(!todos.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }
        return Mono.just(todos.get(id));
    }

    @PatchMapping("/{id}")
    public Mono<Todo> update(@PathVariable String id, @RequestBody Mono<Todo> todo) {
        if(!todos.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }

        return todo.map(it -> {
            if(!ObjectUtils.isEmpty(it.getCompleted())) {
                todos.get(id).setCompleted(it.getCompleted());
            }
            if(!StringUtils.isEmpty(it.getTitle())){
                todos.get(id).setTitle(it.getTitle());
            }
            return todos.get(id);
        });
    }

    @DeleteMapping("/")
    public void delete() {
        todos.clear();
    }

    @DeleteMapping("/{id}")
    public Mono<Todo> delete(@PathVariable String id) {
        if(!todos.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }
        return Mono.just(todos.remove(id));
    }

    @GetMapping("/limit")
    public Mono<Limit> getLimit() {
        return Mono.just(Limit.builder().size(this.todos.size()).limit(this.properties.getApi().getLimit()).build());
    }

}
