package io.todos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableDiscoveryClient
@EnableWebFluxSecurity
@RefreshScope
@SpringBootApplication
public class WebUI {

    public static void main(String[] args) {
		SpringApplication.run(WebUI.class, args);
	}

	@Value("${todos.webui.placeholder:cf push something?}")
	private String placeholder;

    @Value("${spring.security.user.name}")
    private String username;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("placeholder", placeholder);
        model.addAttribute("username", username);
        return "index";
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange().anyExchange().permitAll().and().httpBasic().disable().csrf().disable().build();
    }
}




