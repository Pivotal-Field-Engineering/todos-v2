# Todos Backing API

A sample Spring Boot app that implement a Todo API.

* [Spring Boot](https://spring.io/projects/spring-boot) for app bits

Todos API works with this [UI](https://github.com/corbtastik/todos-webui) but can be used by itself.  If you're interested in running this app as a backend to the [UI](https://github.com/corbtastik/todos-webui) then start with this [repo](https://github.com/corbtastik/todos-edge).

Domain and core API

```java
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class Todo implements Serializable {
        private String id;
        private String title;
        private Boolean completed = Boolean.FALSE;
    }
    @PostMapping("/")
    public Todo create(@RequestBody Todo todo) { }
    @GetMapping("/{id}")
    public Todo retrieve(@PathVariable String id) { }
    @PatchMapping("/{id}")
    public Todo update(@PathVariable String id, @RequestBody Todo todo) { }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { }

```

## Build

This project was created from the [Spring Initialzr](https://start.spring.io) as a Java 1.8, Maven build project.  You can build locally using the [maven wrapper](https://github.com/takari/maven-wrapper) (`mvnw`).

**Tomcat is default**
```bash
./mvnw clean package
# target/todos-api-1.0.0.SNAP.jar
```

**Build with Jetty**
```bash
./mvnw clean package -P jetty
# target/todos-api-1.0.0.SNAP.jar
```

**Build with Undertow**
```bash
./mvnw clean package -P undertow
# target/todos-api-1.0.0.SNAP.jar
```

## Run on PCF

1. Consider forking [this project](https://github.com/corbtastik/todos-api) then clone to dev machine
1. cd into project
1. mvnw clean package
1. modify `manifest.yml` for your cloudfoundry tastes (custom route perhaps?)
1. login to PCF (or [PWS](https://run.pivotal.io/))
1. cf push (awwwweee yeah)

## Local

You can clone, build, run then access ``localhost:8080`` or change the port.

```bash
java -jar ./target/todos-api-1.0.0.SNAP.jar \
  --server.port=whatever
```

## Run with Remote Debug  

```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9111,suspend=n \
  -jar target/todos-api-1.0.0.SNAP.jar
```

## Todo(s) Properties

`TodosProperties` contains a couple application properties.

```bash
# limit number of todos to put in map
todos.api.limit=1024
# use 8 char random string for id or 36 (uuid)
todos.ids.tiny-id=true
```

## Verify

Once Todo(s) API is running access it directly using [cURL](https://curl.haxx.se/) or [HTTPie](https://httpie.org/) to perform CRUD operations.


```bash
> http :8080/ title="make bacon pancakes"
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

{
    "completed": false,
    "id": "c81d4e2e",
    "title": "make bacon pancakes"
}
```

## Spring Boot references:

1. [Dependency Management in Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-dependency-management) - exact dependency versions
1. [Spring Boot Dependencies](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-dependencies/pom.xml)
3. [Spring Boot Starters](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-starter) - this app uses `spring-boot-starter-web`, `spring-boot-starter-actuator` and `spring-boot-starter-sleuth`
4. [How to embed Web Servers](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-web-servers.html) - for servlet stack web apps use [tomcat](http://tomcat.apache.org/), [jetty](https://www.eclipse.org/jetty/), or [undertow](http://undertow.io/).  For reactive stack web apps use the previous servers or [netty](https://netty.io/).
1. [Spring Boot Auto Configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-auto-configuration) - `@SpringBootApplication or @EnableAutoConfiguration`
1. [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config)
1. [ConfigurationProperties](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config-typesafe-configuration-properties)
1. [`@ConfigurationProperties vs @Value`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config-vs-value)
1. [Spring MVC Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-auto-configuration)
1. [Spring Boot Security](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security)
1. [Testing Spring Boot Applications](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-testing-spring-boot-applications)
1. [Actuator Endpoints](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints)
