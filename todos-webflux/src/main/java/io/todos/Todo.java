package io.todos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Todo {
    private String id;
    private String title = "";
    private Boolean completed = Boolean.FALSE;
}
