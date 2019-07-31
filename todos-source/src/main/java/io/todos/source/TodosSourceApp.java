package io.todos.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Streams apps are plain ole Spring Boot apps that
 * declare a "binding" and "channels" to communicate on.
 *
 * Sources are responsible for "sourcing" data to be consumed
 * by a Processors and/or Sinks.
 *
 * This Source simply published an event for every Todo
 * POST'ed to the context root of this app.
 *
 * @author corbs
 */
@SpringBootApplication
@RestController
@EnableBinding(TodosSourceApp.SourceChannels.class)
public class TodosSourceApp {

    private static final Logger LOG = LoggerFactory.getLogger(TodosSourceApp.class);

    /**
     * Sources have outputs
     */
    interface SourceChannels {
        @Output
        MessageChannel output();
    }

    private SourceChannels channels;

    @Autowired
    public TodosSourceApp(SourceChannels channels) {
        this.channels = channels;
    }

    @PostMapping("/")
    public Todo source(@RequestBody Todo todo) {
        if(todo.getId() == null) {
            todo.setId(UUID.randomUUID().toString());
        }

        this.channels.output().send(new GenericMessage<>(todo));
        LOG.info("Publishing event for " + todo.toString());
        return todo;
    }

    public static void main(String[] args) {
        SpringApplication.run(TodosSourceApp.class, args);
    }
}




