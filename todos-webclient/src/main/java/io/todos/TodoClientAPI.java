package io.todos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

/**
 * WebClient uses the same codecs as WebFlux server apps.  It shares base packaging
 * and common APIs with WebFlux.  WebClient features.
 *
 * 1. non-blocking
 * 2. reactive
 * 3. functional API
 * 4. sync and async
 * 5. streaming upload and download to WebFlux enabled server
 */
@RestController
public class TodoClientAPI {

    private static final Logger LOG = LoggerFactory.getLogger(TodoClientAPI.class);

    private final WebClient webClient;

    @Autowired
    public TodoClientAPI(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Post WebClient.retrieve() example
     * @param todo
     * @return
     */
    @PostMapping("/")
    public Mono<Todo> create(@RequestBody Mono<Todo> todo) {

        Mono<Todo> mono = webClient.post()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(todo, Todo.class)
                .retrieve()
                .bodyToMono(Todo.class);

        return mono;
    }

    @GetMapping("/")
    public Flux<Todo> retrieveAll() {

        Flux<Todo> flux = webClient.get()
            .uri("/")
            .retrieve()
            .bodyToFlux(Todo.class);

        return flux;
    }

    /**
     * Get WebClient.exchange() example
     * @return
     */
    @GetMapping("/{id}")
    public Mono<Todo> retrieve(@PathVariable String id) {

        Mono<Todo> mono = webClient.get()
            .uri("/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response -> {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%d", id));
            })
            .bodyToMono(Todo.class);

        return mono;
    }

    @DeleteMapping("/")
    public Mono<Void> deleteAll() {
        return webClient.delete()
            .uri("/")
            .retrieve().bodyToMono(Void.class);
    }

    @DeleteMapping("/{id}")
    public Mono<Todo> delete(@PathVariable String id) {
        return webClient.delete()
            .uri("/{id}", id)
                .retrieve().bodyToMono(Todo.class);
    }

    @PatchMapping("/{id}")
    public Mono<Todo> update(@PathVariable String id, @RequestBody Mono<Todo> todo) {
        Mono<Todo> mono = webClient.patch()
            .uri("/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(todo, Todo.class)
            .retrieve()
            .bodyToMono(Todo.class);

        return mono;
    }

}
