package io.todos.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@RestController
public class TodosAPI {

    private static final Logger LOG = LoggerFactory.getLogger(TodosAPI.class);

    private final Map<String, Todo> todos = Collections.synchronizedMap(new LinkedHashMap<>());

    private final TodosProperties properties;

    @Autowired
    public TodosAPI(TodosProperties properties) {
        LOG.info("TodosAPI booting with todos.api.limit=" + properties.getApi().getLimit());
        this.properties = properties;
    }

    @GetMapping("/")
    public List<Todo> retrieveAll() {
        LOG.debug("Retrieving all todos as a List<Todo> of size " + todos.size()
            + " todos.api.limit=" + properties.getApi().getLimit());
        return new ArrayList<>(todos.values());
    }

    @PostMapping("/")
    public Todo create(@RequestBody Todo todo) {
        if(todos.size() < properties.getApi().getLimit()) {
            LOG.debug("Todos list size=" + todos.size() + " is less than todos.api.limit=" + properties.getApi().getLimit());
            if(properties.getIds().getTinyId()) {
                LOG.debug("Using tinyId generation");
                todo.setId(UUID.randomUUID().toString().substring(0, 8));
            } else {
                LOG.debug("Using uuId generation");
                todo.setId(UUID.randomUUID().toString());
            }
            LOG.debug("Generated todo.id=" + todo.getId());
            LOG.trace("Saving Todo " + todo + " into map");
            todos.put(todo.getId(), todo);
            return todos.get(todo.getId());
        } else {
            LOG.error("Limit reached, todos list size=" + todos.size()
                + " is >= todos.api.limit=" + properties.getApi().getLimit());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                format("todos.api.limit=%d, todos.size()=%d", properties.getApi().getLimit(), todos.size()));
        }
    }

    @DeleteMapping("/")
    public void deleteAll() {
        LOG.info("Removing ALL " + todos.size() + " todos.");
        todos.clear();
    }

    @GetMapping("/{id}")
    public Todo retrieve(@PathVariable String id) {
        if(!todos.containsKey(id)) {
            LOG.warn("Whatcha talkin bout Willis?  todo.id=" + id + " NOT FOUND.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }
        LOG.trace("Retrieving todo.id=" + id);
        return todos.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        if(!todos.containsKey(id)) {
            LOG.warn("Whatcha talkin bout Willis?  Can't delete a todo that doesn't exist todo.id=" + id);
            return;
        }
        LOG.info("Removing todo.id=" + id);
        todos.remove(id);
    }

    @PatchMapping("/{id}")
    public Todo update(@PathVariable String id, @RequestBody Todo todo) {
        if(todo == null) {
            LOG.error("Todo RequestBody can't be null...inconceivable!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "todo request body can't be null");
        }
        if(!todos.containsKey(id)) {
            LOG.warn("Whatcha talkin bout Willis?  Can't update a todo that doesn't exist todo.id=" + id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, format("todo.id=%s", id));
        }
        Todo current = todos.get(id);
        LOG.trace("Pulled current todo.id=" + current.getId() + " from todos List.");
        if(!ObjectUtils.isEmpty(todo.getComplete())) {
            LOG.trace("Updating todo.id=" + current.getId() + " complete field from "
                + current.getComplete() + " to " + todo.getComplete());
            current.setComplete(todo.getComplete());
        }
        if(!StringUtils.isEmpty(todo.getTitle())){
            LOG.trace("Updating todo.id=" + current.getId() + " title field from "
                    + current.getTitle() + " to " + todo.getTitle());
            current.setTitle(todo.getTitle());
        }
        LOG.trace("Returning updated todo for todo.id=" + current.getId());
        return current;
    }

    @GetMapping("/limit")
    public Limit getLimit() {
        LOG.info("Checking Limit, todos.size=" + this.todos.size() + " todos.api.limit=" + properties.getApi().getLimit());
        return Limit.builder().size(this.todos.size()).limit(properties.getApi().getLimit()).build();
    }

    @GetMapping("/kill")
    public void kill() {
        LOG.debug("Killing the process.  BYE!!");
        System.exit(1);
    }
}
