## Audience

This content is intended for anyone wanting a sound understanding of Spring Boot on PCF or for anyone wanting to hack around a simple model and get to know salient features of both.  

## PreReqs

### Development

Each project can be cloned, built and pushed to [PCF](https://pivotal.io/platform) individually.  All that's required to work with projects locally are listed below.

* Java 8 JDK ([sdkman](https://sdkman.io/sdks#java) is a nice dev option)
    * Be able to run `java` and `javac` from cli

```bash
> java -version
openjdk version "1.8.0_212"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_212-b03)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.212-b03, mixed mode)

> javac -version
javac 1.8.0_212
```

* [Git client](https://git-scm.com/downloads) to clone projects
* [Cloud Foundry CLI](https://github.com/cloudfoundry/cli) - install for your platform.  If you have access to [Pivotal Apps Manager](https://docs.pivotal.io/pivotalcf/console/index.html) you can download the latest cf-cli from the Tools menu.  Check cf-cli version.

```bash
> cf -v
cf version 6.46.0+29d6257f1.2019-07-09
```
* A Browser - [Chrome Recommended](https://www.google.com/chrome/)

* [Curl](https://curl.haxx.se/) or [Httpie](https://httpie.org/) - for being able to manually send various api/app requests.  [Post Man](https://www.getpostman.com/) or any http client you're comfortable with is fine.

    ```bash
    # for example use Httpie to update a todo
    http PATCH myscs-todos-app.apps.retro.io/f76bebbe \
        title="WATCH all-star game #mlb"
    # or list application routes in spring-cloud-gateway component
    http foo-todos-edge.apps.retro.io/actuator/gateway/routes
    # or curl to encrypt text via spring-cloud config-server
    curl -H "Authorization: $(cf oauth-token)" https://config-721a0c02-2ec8-466b-bead-fd127b72d464.apps.retro.io/encrypt -d 'Howdy' -k
    ```

* An editor or IDE - All projects were created from the [Spring Initialzr](https://start.spring.io) as Maven for build and Java or Kotlin as the language.  Any editor or IDE will do.  You can use IntelliJ or another IDE such as Eclipse and the source syntax highlighting might show red on generated POJO methods (such as `todos.getTitle()`) unless a Lombok Plugin is added.  In any case you should still be able to edit code and do IDE'less maven builds with or without an IDE plugin...that said make your IDE happy :)

    * [IntelliJ Lombok Plugin](https://projectlombok.org/setup/intellij)
    * [Eclipse and Spring Tools Suite Lombok Plugin](https://projectlombok.org/setup/eclipse)
    * [Visual Studio Code Lombok Plugin](https://projectlombok.org/setup/vscode)


    ![](lombok-plugin.png)

* Connectivity to Maven and Spring Repositories - you may need to configure a [Maven Proxy](https://CHANGEME)

* (Optional) Maven 3 (try [sdkman](https://sdkman.io/sdks#maven)) - all projects use the [Maven Wrapper](https://github.com/takari/maven-wrapper) so maven installation is optional.
* (Optional) [Make](https://www.gnu.org/software/make/) - for quick clone and other ready made commands

### Pivotal Cloud Foundry

1. [Pivotal Application Service 2.5 and up](https://docs.pivotal.io/pivotalcf/2-5)
1. [MySQL for PCF 2.5 and up](https://docs.pivotal.io/p-mysql/2-5/index.html)
1. [RabbitMQ for PCF 1.15 and up](https://docs.pivotal.io/rabbitmq-cf/1-15/index.html)
1. [Redis for PCF 2.0 and up](https://docs.pivotal.io/redis/2-0/index.html)
1. [Spring Cloud Services for PCF 2.0.x](https://docs.pivotal.io/spring-cloud-services/2-0/common/index.html)
1. An account on PCF with Space Developer access
1. This sample set consumes at a minimum [5](https://www.youtube.com/watch?v=k6OWSf8_5J4) SIs
    * 1 MySQL SI - todos-mysql, todos-sink
    * 1 Redis SI - todos-redis, todos-app
    * 1 RabbitMQ SI - todos-app, todos-processor, todos-sink
    * 1 Config Server SI - todos-*
    * 1 Service Registry SI - todos-*

## Projects Setup

Project setup is pretty straightforward as each sample is [standard spring boot](https://start.spring.io) with maven. All applications are modules within the `todos-apps` repository: [todos-apps](https://github.com/Pivotal-Field-Engineering/todos-apps)

After you clone or unzip the repo your directory should look like so...

```bash
azwickey@Adams-MBP-3  ~/todos-apps   master  la -al

drwxr-xr-x  14 azwickey  staff   448B Jul 24 15:30 .
drwxr-xr-x   7 azwickey  staff   224B Jul 24 13:41 ..
drwxr-xr-x  14 azwickey  staff   448B Jul 24 15:44 .git
-rw-r--r--   1 azwickey  staff   393B Jul 24 14:26 .gitignore
-rw-r--r--   1 azwickey  staff   1.2K Jul 24 13:43 README.md
drwxr-xr-x   4 azwickey  staff   128B Jul 24 15:20 pace
-rw-r--r--@  1 azwickey  staff   2.7K Jul 24 14:26 pom.xml
drwxr-xr-x  14 azwickey  staff   448B Jul 24 15:30 todos-api
drwxr-xr-x   8 azwickey  staff   256B Jul 24 14:27 todos-docs
drwxr-xr-x  13 azwickey  staff   416B Jul 24 15:30 todos-edge
drwxr-xr-x  15 azwickey  staff   480B Jul 24 15:30 todos-webui
```

### Domain Model

All the samples in this workshop center around the `Todo` model and hence most are named `todos-blah-blah-blah`.  Each project scratches a particular Spring Boot + PCF itch but all share this simple `Todo` type.

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Todo implements Serializable {
    private String id;
    private String title;
    private Boolean complete = Boolean.FALSE;
}
```

*However* it should be stated that each project maintains it's own implementation of this tiny model.  This gist is a Todo is a String id, String title and Boolean complete flag and that's it.  String as id perhaps is a good talking point around how id(s) should be typed and managed for certain use-cases.  Also considerations around what really needs an id anyway, who should own the id and such.  This sample set uses String (UUID) as an id and app code "owns" dealing out new ones...database id generation isn't used.

**Apropos**

* [`java.util.UUID`](https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html)
* [Mongo ObjectId](https://docs.mongodb.com/manual/reference/method/ObjectId/)
* [Mongo Blog Post](https://www.mongodb.com/blog/post/generating-globally-unique-identifiers-for-use-with-mongodb)
* [Sonyflake distributed ids](https://github.com/sony/sonyflake)  

### todos-api

[Todo(s) API](https://github.com/Pivotal-Field-Engineering/todos-apps/tree/master/todos-api) is a sample Spring Boot service that uses `spring-boot-starter-web` to implement a Spring MVC based REST API for `Todo(s)`.

This is nice Spring Boot ice-breaker app and hacks at core Spring Boot features such as using the Spring Boot Starter pom, Auto-configuration, Property Sources, Configuration Properties, embedded Containers and Logging.  During the workshop we'll use Spring Cloud to connect persistence backends that implement the same HTTP CRUD API.

**Talking Points**

* Spring Boot and its advantages
* Spring Boot Starter POM
* Spring Boot Auto Configuration
* Spring Boot Property Sources
* Spring Boot embedded Containers
* Spring Boot logging
* Spring Boot Web stacks (Spring WebMVC and Spring WebFlux)
* [Spring Framework](https://spring.io/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Java Buildpack](https://github.com/cloudfoundry/java-buildpack)
* Spring Auto Reconfiguration - key is understanding the levels of auto-configuration at play on PCF

### todos-edge

[Todo(s) Edge](https://github.com/Pivotal-Field-Engineering/todos-apps/tree/master/todos-edge) is an edge for other Todo apps and serves as a client entry-point into app functionality, implemented using [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway).  The source is Kotlin however there's remarkably little code as the gateway is virtually all configuration.  By default the edge is configured to route traffic sent to `/todos/` to a backing API of some sort while root traffic routes to the UI.  During certain sections of the workshop we'll use Spring Cloud to control where the edge sends traffic.

**Talking Points**

* Kotlin
* Kotlin support in Spring
* Spring WebFlux
* Spring Cloud Gateway
* Netty

### todos-webui

A sample frontend [Vue.js](https://vuejs.org/) app wrapped in Spring Boot goodness.

* [Spring Boot](https://spring.io/projects/spring-boot) for app bits, using webflux runtime
* [Vue.js](https://vuejs.org/) for frontend, inspired by [TodoMVC Vue App](http://todomvc.com/examples/vue/), difference is this one is vendored as a Spring Boot app and calls a backing endpoint (``/todos``)

This application assumes the ``/todos`` endpoint is exposed from the same "origin".  Because of this its best to use this application behind the [todos-edge](#todos-edge) which will serve as a gateway and single origin to the client for both loading ``todos-webui`` and for proxying API calls to ``/todos``.

![Todo(s) WebUI](todos-webui.png)

### todos-mysql

Inevitably you'll need to implement Microservices to talk with Databases. [Todo(s) MySQL](https://github.com/corbtastik/todos-mysql) contains a Microservice implemented in Kotlin using [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/) and [Spring Cloud](https://spring.io/projects/spring-cloud) under-pinnings, [Spring Data JPA](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-sql.html#boot-features-jpa-and-spring-data) for data binding and [Flyway](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-execute-flyway-database-migrations-on-startup) for database initialization.  It can be used as a standalone data service or as a backend for a Todo app.

> [Spring Framework 5](https://spring.io/blog/2017/01/04/introducing-kotlin-support-in-spring-framework-5-0) added support for [Kotlin](https://kotlinlang.org/). Developers can now implement Microservices in [Java](https://en.wikipedia.org/wiki/Java_(programming_language)), [Groovy](https://groovy-lang.org/) and [Kotlin](https://kotlinlang.org/). Kotlin is [starting to show up in development communities](https://www.thoughtworks.com/radar/languages-and-frameworks/kotlin) given most like the simplified syntax. If you're a Java developer you'll get up to speed on Kotlin quickly because it's very Java like but with [lots of simplifications](https://kotlinlang.org/docs/reference/comparison-to-java.html).

Todo(s) Data uses Flyway to handle database schema creation and versioning. Using Flyway from Spring Boot starts with declaring a dependency on `org.flywaydb:flyway-core` in `pom.xml` and Spring Boot will Auto Configure on startup.  Spring profiles are used to boot into different database context, the default profile initiates H2 while the "cloud" profile used MySQL.

**Talking Points**

* Kotlin
* Spring Data
* Spring Data JPA
* Flyway
* MySQL for PCF

### todos-redis

[Todo(s) Redis](https://github.com/corbtastik/todos-redis) is a simple [Spring Data Redis](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-nosql.html#boot-features-redis) backend for a Todo API.  It's used as the default "caching" layer in the workshop sample set.

**Talking Points**

* [Redis](https://redis.io/)
* [Redis Hashes](https://redis.io/topics/data-types)
* [Redis cli](https://redis.io/topics/rediscli)
* [Redis for PCF](https://docs.pivotal.io/redis/index.html)
* [Spring Data Redis](https://docs.spring.io/spring-data/redis/docs/reference/html/)
* [Caching Use Cases and Patterns](https://content.pivotal.io/blog/an-introduction-to-look-aside-vs-inline-caching-patterns)

### todos-app

Todo(s) App is a composite backend for a fully functional Todo app, it contains look-aside caching and business logic and uses Spring Cloud for app to app connectivity.  We'll see how to configure a fully functional Todo application using this backend which integrates with a System of Record (Sor) service and a Cache service. All enabled with Spring Cloud, MySQL for PCF and Redis for PCF.

**Talking Points**

* [Spring Cloud Services for PCF](https://docs.pivotal.io/spring-cloud-services/common/index.html)
* [MySQL for PCF](https://docs.pivotal.io/p-mysql/index.html)
* [Redis for PCF](https://docs.pivotal.io/redis/index.html)
* Configuration Server and Service Registry
* RestTemplate and Client Side Load-balancing
* [Pivotal Blog - Caching Patterns](https://content.pivotal.io/blog/an-introduction-to-look-aside-vs-inline-caching-patterns)

