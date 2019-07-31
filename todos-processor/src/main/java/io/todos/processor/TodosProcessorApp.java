package io.todos.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Processors are Spring Boot apps that declare a "binding"
 * and "channels" to use for communication.  Processors will
 * take one or more "inputs" and produce one or more "outputs".
 *
 */
@SpringBootApplication
@EnableBinding(TodosProcessorApp.Channels.class)
public class TodosProcessorApp {

    private static final Logger LOG = LoggerFactory.getLogger(TodosProcessorApp.class);

    /**
     * Processors have inputs and outputs
     */
    interface Channels {
        @Output
        MessageChannel output();
        @Input
        SubscribableChannel input();
    }

    private Channels channels;

    @Autowired
    public TodosProcessorApp(Channels channels) {
        this.channels = channels;
    }

    /**
     * Listen on the "input" and publish events on "output" channels
     * @param todo
     * @return
     */
    @StreamListener("input")
    @Output("output")
    public Todo transform(@Payload Todo todo) {
        StringTokenizer tokenizer = new StringTokenizer(todo.getTitle());
        Set<String> hashTags = new HashSet<>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.startsWith("#")) {
                LOG.info("hashtag found" + token);
                hashTags.add(token);
            }
        }
        return Todo.builder()
            .id(todo.getId())
            .completed(todo.getCompleted())
            .title(todo.getTitle())
            .hashtags(hashTags)
            .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TodosProcessorApp.class, args);
    }
}




