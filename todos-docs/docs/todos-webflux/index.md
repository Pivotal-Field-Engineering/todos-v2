# README

## Todo(s) Webflux API  

Howdy and welcome.  This repository contains a Reactive API implemented using [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Cloud](https://projects.spring.io/spring-cloud/), it uses [Spring WebFlux](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html) to map endpoints to methods and functions as a back-end for the [Todo(s) UI](https://github.com/corbtastik/todos-ui).

### Primary dependencies

* Spring Boot Starter WebFlux (non-blocking API)
* Spring Boot Actuators (ops endpoints)
* Spring Cloud Netflix Eureka Client (service discovery)
* Spring Cloud Config Client (central config)
* Spring Cloud Sleuth (request tracing)

This API is part of the [Todo collection](https://github.com/corbtastik/todos-ecosystem) of Microservices.  For a full listing [checkout](https://github.com/corbtastik/todos-ecosystem).

### Spring Boot Reactive Stack

This Microservice is similar to Todo(s) API in the sense they share common framework interfaces, annotations, and the JVM as a runtime.  Beyond that they're quite different.

* Todo(s) WebFlux is reactive where as Todo(s) API is imperative
* Todo(s) WebFlux is non-blocking where as Todo(s) API is blocking
* Todo(s) WebFlux is asynchronous whereas Todo(s) API is synchronous

With the release of Spring 5 and Spring Boot 2.0 the framework offers a sibling Web Stack thats fully non-blocking and runs on [Netty](http://netty.io/).  Traditional Spring Boot Microservices run on Servlet Containers but developers can choose which embedded container to use, for example ``spring-boot-starter-jetty`` or ``spring-boot-starter-undertow`` in addition to the default ``spring-boot-starter-tomcat`` which is pulled in by default when ``spring-boot-starter-web`` is included.

**Now** developers have another embedded container stack to use that's reactive and provided by, ``spring-boot-starter-webflux``.  The new Reactive Stack takes advantage of multi-core CPU(s) in a manner that allows for a higher number of connections than a Servlet Stack Microservice.

Prior to Spring Boot 2 if a developer wanted to implement a Reactive Microservice they had to step outside the Spring Framework to implement.  Now developers can stay within one Framework and get Reactive support...awe yeah.

### Reactive Stack Hierarchy

App | |  
------------ | ------------- |
Spring Boot 2 | Reactive support from ``spring-boot-starter-webflux``
Spring 5 Framework | Provides WebFlux module
Reactor Core | Core non-blocking libs, see [projectreactor](http://projectreactor.io/)

### Reactive Controller

We can define a Reactive Controller using the same web binding annotations, ``@RestController`` and ``@RequestMapping`` that we know and love from SpringMVC (yeah thats pretty cool and exactly what a framework is suppose to enable wink wink).  In fact if you look at the ``@RestController`` for [Todo(s) API](https://github.com/corbtastik/todos-api) you'll see the same framework annotations on the non-reactive implementation.

The key difference is how we type inputs and outputs from the ``@RestController``, in the Reactive Stack the types are Non-blocking by design so we don't merely return rendered values we return "promises" to provide rendered values.  The reactive types are core to [reactor](http://projectreactor.io/) and are more accurately called ``Publisher(s)`` and they "publish" to ``Subscriber(s)`` who have explicitly subscribed and asked for information.  These are types specific to the [Reactive Streams](http://www.reactive-streams.org/) spec, a spec that's implemented by [reactor](http://projectreactor.io/).

### References

[Web Reactive](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html)
