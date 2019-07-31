package io.todos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@EnableConfigurationProperties(TodosProperties.class)
@SpringBootApplication
public class TodosApp {

	public static void main(String[] args) {
		SpringApplication.run(TodosApp.class, args);
	}
}
