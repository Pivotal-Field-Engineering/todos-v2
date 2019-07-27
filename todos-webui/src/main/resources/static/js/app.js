/*global Vue */
(function (exports) {
    'use strict';
    Vue.use(VueResource);

    const filters = {
        all: function (todos) {
            return todos;
        },
        active: function (todos) {
            return todos.filter(function (todo) {
                return !todo.complete;
            });
        },
        completed: function (todos) {
            return todos.filter(function (todo) {
                return todo.complete;
            });
        }
    };

    exports.app = new Vue({
        // the root element that will be compiled
        el: '#todoapp',
        // app initial state
        data: {
            todos: [],
            metadata: [],
            buildInformation: {},
            newTodo: '',
            metaSearch: '',
            editedTodo: null,
            visibility: 'all',
            offline: false,
            activetab: 1
        },
        // watch todos change and save via API
        watch: {
            todos: {
                deep: true,
                handler: function(values) {
                    const self = this;
                    values.forEach(todo => {
                        if(todo.id && !self.offline) {
                            console.log("DEBUG|watchTodos|Patching todo " + todo);
                            Vue.http.patch('/todos/' + todo.id,todo);
                        }
                    });
                }
            }
        },
        computed: {
            filteredTodos() {
                return filters[this.visibility](this.todos);
            },
            remaining() {
                return filters.active(this.todos).length;
            },
            allDone: {
                get: function () {
                    return this.remaining === 0;
                },
                set: function (value) {
                    this.todos.forEach(function (todo) {
                        todo.complete = value;
                    });
                }
            },
            filteredMetadata() {
                const searchFilter = meta =>
                    meta.property.includes(this.metaSearch)
                        ||
                    meta.value.includes(this.metaSearch);
                return this.metadata.filter(searchFilter);
            }
        },
        // methods that implement data logic.
        // note there's no DOM manipulation here at all.
        methods: {
            pluralize: function (word, count) {
                return word + (count === 1 ? '' : 's');
            },
            addTodo: function () {
                console.log("DEBUG|addTodo|adding new todo");
                const value = this.newTodo && this.newTodo.trim();
                if (!value) {
                    return;
                }
                this.createTodo({
                    title: value,
                    complete: false
                });
                this.newTodo = '';
            },
            //ERROR, WARN, INFO, DEBUG, or TRACE.
            createTodo: function(todo) {
                const self = this;
                if(!self.offline) {
                    console.log("DEBUG|createTodo|API online");
                    console.log("DEBUG|createTodo|Posting new todo " + todo);
                    Vue.http.post('/todos/', {
                        title: todo.title,
                        complete: todo.complete
                    }).then(response => {
                        console.log("DEBUG|createTodo|Response OK");
                        self.todos.unshift(response.body);
                    });
                } else {
                    console.log("WARN|createTodo|API OFFLINE saving to local storage");
                    self.todos.unshift(todo);
                }
            },
            removeTodo: function (todo) {
                const self = this;
                if(!self.offline) {
                    console.log("DEBUG|removeTodo|API online");
                    console.log("DEBUG|removeTodo|Removing todo " + todo.id);
                    Vue.http.delete( '/todos/' + todo.id).then(() => {
                        console.log("TRACE|removeTodo|Re-indexing todos");
                        const index = self.todos.indexOf(todo);
                        self.todos.splice(index, 1);
                        console.log("TRACE|removeTodo|Re-indexing complete");
                    });
                } else {
                    console.log("DEBUG|removeTodo|API OFFLINE removing from local storage");
                    const index = self.todos.indexOf(todo);
                    self.todos.splice(index, 1);
                }
            },
            editTodo: function (todo) {
                if(todo.complete) {
                    console.log("ERROR|editTodo|Can't edit a complete todo amigo");
                    return;
                }
                console.log("DEBUG|editTodo|Editing todos " + todo.id);
                this.beforeEditCache = todo.title;
                this.editedTodo = todo;
            },
            doneEdit: function (todo) {
                if (!this.editedTodo) {
                    return;
                }
                this.editedTodo = null;
                todo.title = todo.title.trim();
                if (!todo.title) {
                    this.removeTodo(todo);
                }
                console.log("DEBUG|doneEdit|Editing complete " + todo.id);
            },
            cancelEdit: function (todo) {
                this.editedTodo = null;
                todo.title = this.beforeEditCache;
                console.log("DEBUG|cancelEdit|Editing cancelled " + todo.id);
            },
            removeCompleted: function () {
                this.todos = filters.active(this.todos);
            }
        },
        // run before mounting to see if API is enabled or not
        beforeMount() {
            const self = this;
            Vue.http.get('/todos/').then(response => {
                const list = JSON.parse(response.bodyText);
                list.forEach(item => {
                    self.todos.unshift(item);
                });
                console.log("INFO|beforeMount|/todos is online, saving to API");
            }, response => {
                if(response.status===404) {
                    // api offline, save local only
                    console.log("WARN|beforeMount|/todos is offline, saving to local storage");
                    self.offline = true;
                }
            });
            Vue.http.get('/metadata').then(response => {
                const list = JSON.parse(response.bodyText);
                list.forEach(item => {
                    self.metadata.push(item);
                });
                console.log("INFO|beforeMount|/metadata is online");
            }, response => {
                if(response.status===404) {
                    console.log("WARN|beforeMount|/metadata is offline");
                }
            });
            Vue.http.get('/about').then(response => {
                self.buildInformation = JSON.parse(response.bodyText);
                console.log("INFO|beforeMount|/about is online");
            }, response => {
                if(response.status===404) {
                    console.log("WARN|beforeMount|/about is offline");
                }
            });
        },
        // a custom directive to wait for the DOM to be updated
        // before focusing on the input field.
        // http://vuejs.org/guide/custom-directive.html
        directives: {
            'todo-focus': function (el, binding) {
                if (binding.value) {
                    el.focus();
                }
            }
        }
    });

})(window);
