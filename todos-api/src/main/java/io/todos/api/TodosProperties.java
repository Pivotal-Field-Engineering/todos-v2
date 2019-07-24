package io.todos.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("todos")
public class TodosProperties {

    private final Api api = new Api();

    private final Ids ids = new Ids();

    public Api getApi() {
        return this.api;
    }

    public Ids getIds() {
        return this.ids;
    }

    public static class Api {
        private Integer limit = 1024;
        public void setLimit(Integer limit) {
            this.limit = limit;
        }
        public Integer getLimit() {
            return this.limit;
        }
    }

    public static class Ids {
        private Boolean tinyId = true;
        public void setTinyId(Boolean value) {
            this.tinyId = value;
        }
        public Boolean getTinyId() {
            return this.tinyId;
        }
    }
}
