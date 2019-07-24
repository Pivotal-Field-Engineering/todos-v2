# Todos WebUI

A sample frontend app wrapped in Spring Boot

* [Spring Boot](https://spring.io/projects/spring-boot) for app bits, using webflux runtime
* [Vue.js](https://vuejs.org/) for frontend, inspired by [TodoMVC Vue App](http://todomvc.com/examples/vue/), difference is this one is vendored as a Spring Boot app and calls a backing endpoint (``/todos``)

This application assumes the ``/todos`` endpoint is exposed from the same "origin".  Because of this its best to use this application behind the [todos-edge](https://github.com/corbtastik/todos-edge) which will serve as a gateway and single origin to the client for both loading ``todos-webui`` and for proxying API calls to ``/todos``.

Spring Boot property handling makes it easy to implement custom properties, for example ``todos.webui.placeholder`` can be set to whatever you'd like and it will show up on the UI.

## Run on PCF

1. Consider forking [this project](https://github.com/corbtastik/todos-webui) then clone to dev machine
1. cd into project
1. mvnw clean package
1. modify ``manifest.yml`` for your cloudfoundry tastes (custom route perhaps?)
1. login to PCF (or [PWS](https://run.pivotal.io/))
1. cf push (awwwweee yeah)

To have data sent to the backend run this app with [todos-edge](https://github.com/corbtastik/todos-edge) and access UI via the edge's published endpoint (See [todos-edge README](https://github.com/corbtastik/todos-edge)).

Running this app without an edge is fine, however the data will just be saved to local browser storage and bark because you're not plugged up to the api.  In this case the app is essentially "vendoring" a frontend application ([vue.js](https://vuejs.org/),html,css) and serving it over the JVM instead of a plain-ole http-server (such as [apache-httpd](https://httpd.apache.org/) or [nginx](https://www.nginx.com/)).    

## Local

You can clone, build, run then access ``localhost:8080`` or change the port.

```bash
java -jar ./target/todos-webui-1.0.0.SNAP.jar \
  --server.port=whatever
``` 

**WebUI running**

<p align="center">
    <img src="https://github.com/corbtastik/todos-images/raw/master/todos-webui/ui.png" width="640">
</p>


