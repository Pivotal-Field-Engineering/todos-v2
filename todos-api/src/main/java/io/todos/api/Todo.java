package io.todos.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Todo implements Serializable {
    private String id;
    private String title;
    private Boolean complete = Boolean.FALSE;
}