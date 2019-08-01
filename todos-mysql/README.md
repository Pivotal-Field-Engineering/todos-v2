## Todo(s) MySQL

Howdy and welcome...data, data and more data, the world is full of data.  Inevitably you'll need to implement Microservices to connect with Databases.  This repository contains a sample implemented in [Kotlin](https://kotlinlang.org) using [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Data](https://spring.io/projects/spring-data) libraries.

### Primary dependencies

* Spring Boot Starter Web (Embedded Tomcat)
* Spring Boot Actuators (actuator endpoints)
* Spring Data JPA (easy database access)
* Spring Data Rest (generated Rest API for data model)
* Flyway (Database schema migration)

### Kotlin

[Spring 5](http://spring.io/) added support for a 3rd language in the Framework.  Developers can now implement Microservices in [Java](https://en.wikipedia.org/wiki/Java_(programming_language)), [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) and [Kotlin](https://kotlinlang.org).  Kotlin is slowly starting to [show up in development communities](https://www.thoughtworks.com/radar/languages-and-frameworks/kotlin) given most like the simplified syntax.

Kotlin is a statically typed open source language originated by [JetBrains](https://www.jetbrains.com) and now officially supported by Spring.  This sample leverages Kotlin to do what Java can do but with less code.

### Flyway - Database Migrations

This sample uses [Flyway](https://flywaydb.org/) to handle database schema versioning.  Using Flyway from Spring Boot starts with declaring a dependency on ``org.flywaydb:flyway-core`` in ``pom.xml``.  Spring Boot handles [Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-execute-flyway-database-migrations-on-startup) of Flyway on start-up when ``org.flyway:flyway-core`` is on the classpath.

Todo(s) MySQL uses a default Spring Boot/Flyway setup and overrides ``spring.flyway.locations`` for different profiles.  The ``default`` profile ``application.yml`` sets the location for H2 schema while the ``cloud`` profile sets the schema location for MySQL as [MySQL for PCF](https://docs.pivotal.io/p-mysql/2-6/index.html) is assumed to be used with this sample.

```bash
> tree ./src/main/resources
|____application-cloud.yml
|____application.yml
|____db
| |____migration
| | |____h2
| | | |____V1.0.0__create_todos_table.sql
| | |____mysql
| | | |____V1.0.0__create_todos_table.sql
```

If for some reason you need to disable schema migration just override ``spring.flyway.enabled=false``, by default Spring Boot will run ``Flyway.migrate()`` on startup which will run new migrations.

### Todo(s) Data JPA

One of the most underrated features of Spring is the [Spring Data family](https://spring.io/projects/spring-data).  Connecting Microservices to databases and stores is one of the cornerstones of the framework.  Giving developers a similar framework components to access data in a low touch manner.

Our model is the ``Todo`` and since we're using Flyway we're controlling the schema creation through migrations in ``src/main/resources/db/migrations``.  We could allow JPA to create the schema but we're choosing not too as most enterprise shops favor this level of separation.  This finer grained control comes with more responsibility on the developer.  We need to ensure our Object matches the schema, so as a safe guard we enable ``spring.jpa.hibernate.ddl-auto=validate`` which will [halt-and-catch-fire](https://en.wikipedia.org/wiki/Halt_and_Catch_Fire_(TV_series)) should those two be outta wack.

### Todo(s) Data Rest API

Todo(s) MySQL sample uses ``spring-boot-starter-data-rest`` to blanked our data model in a REST API, one that supports paging and sorting (whoop-whoop).

Spring Data Rest will use the Repository and Entity to generate a HAL based api.  Since this sample has a `@RestController` wired to the root `/` context down we'll need to provide another basePath for the generated Spring Data Rest API.  We do that by slipping the basePath under the existing Spring Boot Actuator `/actuator/*` path.

Spring Data Rest API endpoint

```yaml
# application.yml
spring:
  data:
    rest:
      base-path: '/actuator/data' # spring-data-rest generated API here
```

Relevant Spring Data objects

```kotlin
@Entity
@Table(name="todos")
class Todo {
    @Id
    var id: String? = null
    var title: String? = null
    var complete: Boolean = false
    fun complete() {
        this.complete = true
    }
    fun incomplete() {
        this.complete = false
    }
}
@Repository("todosRepo")
interface TodosRepo : PagingAndSortingRepository<Todo, String>
```

### Build

```bash
git clone https://github.com/Pivotal-Field-Engineering/todos-apps.git
cd todos-apps
# to build only the todos-mysql module
./mvnw -pl todos-mysql clean package
```

### Run on PCF

Configure your `manifest.yml` to meet your needs, it's simple enough to create a MySQL Service Instance and bind to it on push like so...

Create the on-demand MySQL Service Instance.

```bash
cf create-service p.mysql db-small todos-database
```

Configure your `manifest.yml`, declaring a binding to the MySQL Service Instance.

```yaml
---
applications:
- name: todos-mysql
  memory: 1G
  path: target/todos-mysql-1.0.0.SNAP.jar
  services:
  - todos-database
  env:
    TODOS_API_LIMIT: 1024
```

```bash
cd todos-apps/todos-mysql
cf push -f manifest.yml # aweeee yeah
```

### Verify

```bash
http todos-mysql.apps.retro.io/
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Vcap-Request-Id: 638993f6-e2d9-40ec-5ab3-286d0ff36d4a

[]
```

### Check Actuator Info

```bash
http todos-mysql.apps.retro.io/actuator/info
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
X-Vcap-Request-Id: 200207f3-96ea-4d81-5b1c-938b6b56483b
{
    "build": {
        "artifact": "todos-mysql",
        "group": "io.todos",
        "name": "todos-mysql",
        "time": "2019-07-01T17:59:43.781Z",
        "version": "1.0.0.SNAP"
    }
}
```

### Check Spring Data Rest API

```bash
http todos-mysql.apps.retro.io/actuator/data/todos/
HTTP/1.1 200 OK
Content-Length: 399
Content-Type: application/hal+json;charset=UTF-8
X-Vcap-Request-Id: abe90cc3-b5e6-4efc-47b6-2eeb08cc98ee
{
    "_embedded": {
        "todos": []
    },
    "_links": {
        "profile": {
            "href": "http://todos-mysql.apps.retro.io/actuator/data/profile/todos"
        },
        "self": {
            "href": "http://todos-mysql.apps.retro.io/actuator/data/todos{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 0,
        "totalPages": 0
    }
}
```

## Next Steps

* Configure as a backend for the todos-webui
* Load data into MySQL via the Spring Data API and Application API
* Fetch data from MySQL via the Spring Data API and Application API
* Switch to `cloud` branch, build and push as part of a Spring Cloud deployment
