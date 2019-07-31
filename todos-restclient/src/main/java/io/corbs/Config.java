package io.corbs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

    @Value("${todos.api.endpoint}")
    private String endpoint;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().rootUri(endpoint).build();
    }
}
