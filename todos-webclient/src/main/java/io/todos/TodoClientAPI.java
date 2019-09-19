package io.todos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
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

    @Autowired
    private ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @Value("${todos.api.service}")
    private String service;

    /**
     * Post WebClient.retrieve() example
     * @param todo
     * @return
     */
    @PostMapping("/")
    public Mono<Todo> create(@RequestBody Mono<Todo> todo) {
        LOG.debug("WebClient calling create on " + service);
        return WebClient.builder().baseUrl(service)
            .filter(lbFunction)
            .build()
            .post()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(todo, Todo.class)
            .retrieve()
            .bodyToMono(Todo.class);
    }

    @GetMapping("/")
    public Flux<Todo> retrieveAll() {
        LOG.debug("WebClient calling retrieveAll on " + service);
        return WebClient.builder().baseUrl(service)
            .filter(lbFunction)
            .build()
            .get()
            .uri("/")
            .retrieve()
            .bodyToFlux(Todo.class);
    }

    /**
     * Get WebClient.exchange() example
     * @return
     */
    @GetMapping("/{id}")
    public Mono<Todo> retrieve(@PathVariable String id) {
        LOG.debug("WebClient calling retrieve on " + service);
        return WebClient.builder().baseUrl(service)
            .filter(lbFunction)
            .build()
            .get()
            .uri("/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response -> {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
            })
            .bodyToMono(Todo.class);
    }

    @DeleteMapping("/")
    public Mono<Void> deleteAll() {
        LOG.debug("WebClient calling deleteAll on " + service);
        return WebClient.builder().baseUrl(service)
            .filter(lbFunction)
            .build()
            .delete()
            .uri("/")
            .retrieve().bodyToMono(Void.class);
    }

    @DeleteMapping("/{id}")
    public Mono<Todo> delete(@PathVariable String id) {
        LOG.debug("WebClient calling delete on " + service);
        return WebClient.builder().baseUrl(service)
            .filter(lbFunction)
            .build()
            .delete()
            .uri("/{id}", id)
                .retrieve().bodyToMono(Todo.class);
    }

    @PatchMapping("/{id}")
    public Mono<Todo> update(@PathVariable String id, @RequestBody Mono<Todo> todo) {
        LOG.debug("WebClient calling update on " + service);
        return WebClient.builder().baseUrl(service)
            .filter(lbFunction)
            .build()
            .patch()
            .uri("/{id}", id)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(todo, Todo.class)
            .retrieve()
            .bodyToMono(Todo.class);
    }
}