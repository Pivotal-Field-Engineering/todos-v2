package io.corbs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class TodoClientApp {

    @Value("${todos.api.endpoint}")
    private String endpoint;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(endpoint)
            .filter((request, next) -> {
                ClientRequest filtered = ClientRequest.from(request)
                    .header("X-TODOS-SERVICE-ID", "todos-webclient").build();
                return next.exchange(filtered);
            })
            .build();
    }

	public static void main(String[] args) {
		SpringApplication.run(TodoClientApp.class, args);
	}
}
