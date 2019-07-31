package io.todos.common;


import org.junit.Assert;
import org.junit.Test;

public class ObjectIdTests {

    @Test
    public void equality() {
        Todo todo1 = new Todo("5d20fcbd6761fa5d7f8c9a9f", "Make coffee", false);
        Todo todo2 = new Todo("5d20fcbd6761fa5d7f8c9a9f", "Make coffee", false);
        Assert.assertEquals(todo1, todo2);
    }
}