package io.todos.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.SubscribableChannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sink apps are Spring Boot apps that declare a "binding"
 * and "channels" to communicate on.
 *
 * Sinks receive data and presumably do something with it,
 * like save to a database, file-system or push events.
 *
 */
@SpringBootApplication
@EnableBinding(TodosSinkApp.SinkChannels.class)
public class TodosSinkApp {

    private static final Logger LOG = LoggerFactory.getLogger(TodosSinkApp.class);

    private Map<String, Set<String>> hashtagIndex = new HashMap<>();

    /**
     * Sinks receive information and thus need an input
     */
    interface SinkChannels {
        @Input
        SubscribableChannel input();
    }

    @StreamListener("input")
    void sink(Todo todo) {
        LOG.info("TodosSinkApp handled todo event " + todo.toString());
        if(!todo.getHashtags().isEmpty()) {
            LOG.info("Hashtags on: " + todo.getId() + " hashtags:" + todo.getHashtags());
            LOG.info("Adding to hashtagIndex");
            updateIndex(todo.getHashtags(), todo);
            LOG.info("Hashtag Index: " + hashtagIndex.toString());
        }
    }

    void updateIndex(Set<String> hashtags, Todo todo) {
        for(String tag : hashtags) {
            if(hashtagIndex.containsKey(tag)) {
                hashtagIndex.get(tag).add(todo.getId());
            } else {
                Set<String> ids = new HashSet<>();
                ids.add(todo.getId());
                hashtagIndex.put(tag, ids);
            }
        }
    }

    public static void main(String[] args) {
		SpringApplication.run(TodosSinkApp.class, args);
	}
}


