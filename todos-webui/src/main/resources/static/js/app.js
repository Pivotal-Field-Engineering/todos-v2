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
            createTodo: function(todo) {
                const self = this;
                if(!self.offline) {
                    Vue.http.post('/todos/', {
                        title: todo.title,
                        complete: todo.complete
                    }).then(response => {
                        self.todos.unshift(response.body);
                    });
                } else {
                    self.todos.unshift(todo);
                }
            },
            removeTodo: function (todo) {
                const self = this;
                if(!self.offline) {
                    Vue.http.delete( '/todos/' + todo.id).then(() => {
                        const index = self.todos.indexOf(todo);
                        self.todos.splice(index, 1);
                    });
                } else {
                    const index = self.todos.indexOf(todo);
                    self.todos.splice(index, 1);
                }
            },
            editTodo: function (todo) {
                if(todo.complete) {
                    return;
                }
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
            },
            cancelEdit: function (todo) {
                this.editedTodo = null;
                todo.title = this.beforeEditCache;
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
                console.log("INFO /todos is online, saving to API");
            }, response => {
                if(response.status===404) {
                    // api offline, save local only
                    console.log("WARN /todos is offline, saving local");
                    self.offline = true;
                }
            });
            Vue.http.get('/metadata').then(response => {
                const list = JSON.parse(response.bodyText);
                list.forEach(item => {
                    self.metadata.push(item);
                });
                console.log("INFO /metadata loaded");
            }, response => {
                if(response.status===404) {
                    console.log("WARN /metadata is offline");
                }
            });
            Vue.http.get('/about').then(response => {
                self.buildInformation = JSON.parse(response.bodyText);
                console.log("INFO /about loaded");
            }, response => {
                if(response.status===404) {
                    console.log("WARN /about is offline");
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
