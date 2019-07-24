package io.todos.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigurationProperties(TodosProperties.class)
public class TodosApp implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(TodosApp.class);

	private final Environment env;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TodosApp.class);
		app.addListeners((ApplicationListener<ApplicationEvent>) applicationEvent -> {
			if(applicationEvent instanceof ApplicationStartingEvent) {
				LOG.info("ApplicationStartingEvent @ " + applicationEvent.getTimestamp());
			} else if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
				LOG.info("ApplicationEnvironmentPreparedEvent @ " + applicationEvent.getTimestamp());
			} else if(applicationEvent instanceof ApplicationPreparedEvent) {
				LOG.info("ApplicationPreparedEvent @ " + applicationEvent.getTimestamp());
			} else if(applicationEvent instanceof ApplicationStartedEvent) {
				LOG.info("ApplicationStartedEvent @ " + applicationEvent.getTimestamp());
			} else if(applicationEvent instanceof ApplicationReadyEvent) {
				LOG.info("ApplicationReadyEvent @ " + applicationEvent.getTimestamp());
			}  else if(applicationEvent instanceof ApplicationFailedEvent) {
				LOG.info("ApplicationFailedEvent @ " + applicationEvent.getTimestamp());
			}
		});
		app.run(args);
	}

	@Autowired
	public TodosApp(ApplicationArguments args, Environment environment) {
		this.env = environment;
		boolean debug = args.containsOption("debug");
		if(debug) {
			LOG.debug("DEBUG Mode for:" + this.env.getProperty("spring.application.name"));
		}
	}

	@Override
	public void run(ApplicationArguments applicationArguments) {
		for (String it : applicationArguments.getNonOptionArgs()) {
			LOG.info(it);
		}
		applicationArguments.getOptionNames().stream().sorted().forEach(option -> {
			LOG.info(option);
			applicationArguments.getOptionValues(option).forEach(value -> {
				LOG.info("\t" + value);
			});
		});
	}
}
