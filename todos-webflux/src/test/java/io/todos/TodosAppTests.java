package io.todos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TodosAppTests {

    @Autowired
    private TodosProperties properties;

    @Test
    public void createDelete() {
        WebTestClient webTestClient = WebTestClient
                .bindToController(new TodosAPI(properties)).build();
        webTestClient.post()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(Todo.builder().title("make bacon pancakes").completed(false).build()), Todo.class)
            .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .consumeWith( body -> {
                    assertThat(body.getResponseBody().getId()).isNotEmpty();
                    assertThat(body.getResponseBody().getTitle()).isEqualTo("make bacon pancakes");
                    assertThat(body.getResponseBody().getCompleted()).isFalse();
                });

        webTestClient.delete()
            .uri("/")
                .exchange()
                .expectStatus().isOk();

    }
}