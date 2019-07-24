# README

## Todo(s) Backing API

Howdy and welcome.  This repository contains a Microservice API implemented in [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Cloud](https://spring.io/projects/spring-cloud).  This API uses Spring MVC to map endpoints to methods using the time tested ``@RestController`` and ``@RequestMapping`` annotations.  The API is the backend for the [Vue.js version](http://todomvc.com/examples/vue/) of [TodoMVC](http://todomvc.com/).  Frontend repo is [here](https://github.com/corbtastik/todos-ui).

### Primary dependencies

* [Spring Boot Starter Web](https://spring.io/projects/spring-boot) - rest api
* [Spring Boot Actuators](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html) - ops endpoints
* [Spring Cloud Netflix Eureka](https://cloud.spring.io/spring-cloud-netflix/) - discovery client
* [Spring Cloud Config Client](https://cloud.spring.io/spring-cloud-config/) - config client
* [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth) - request tracing
* [Swagger API documentation](http://springfox.github.io/springfox/) - API docs with SpringFox

This API is part of the [Todo collection](https://github.com/corbtastik/todos-ecosystem) which are part of a larger demo set used in Cloud Native Developer Workshops.

This example shows how easy it is to implement Microservices using Spring Boot.  If you have zero to little experience with Spring Boot then this example is a good starting point for learning.  The purpose is to implement an API backend for [Todo(s) UI](https://github.com/corbtastik/todos-ui).  By default the API saves Todo(s) in a ``LinkedHashMap`` which is capped at 25 but with Spring Boot Property Sources we can override at startup like so: ``--todos.api.limit=100``.

### API Controller

With ``@RestController`` and ``@RequestMapping`` annotations on a ``class`` we can encapsulates and provide context for an API.  ``TodoAPI`` maps http requests starting with `/todos` to CRUD methods implemented in this class.  The [Todo(s) Data](https://github.com/corbtastik/todos-data) Microservice exposes a similar CRUD API but with zero code from us, it uses Spring Data Rest to blanket a Data Model with a CRUD based API.  Check out [Todo(s) Data](https://github.com/corbtastik/todos-data) for more information on Spring Boot with Spring Data Rest.

```java
@RestController
@RequestMapping("todos")
public class TodosAPI {
    // ...
}
```

### API operations

1. **C**reate a Todo
2. **R**etrieve one or more Todo(s)
3. **U**pdate a Todo
4. **D**elete one or more Todo(s)

```java
    @PostMapping("/")
    public Todo create(@RequestBody Todo todo) { }

    @GetMapping("/{id}")
    public Todo retrieve(@PathVariable Integer id) { }

    @GetMapping("/")
    public List<Todo> retrieve() { }

    @PatchMapping("/{id}")
    public Todo update(@PathVariable Integer id, @RequestBody Todo todo) { }    

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { }

    @DeleteMapping("/")
    public void delete() { }
```

### API documentation

[Swagger](https://swagger.io/) integrates with Spring Boot quite nicely thanks to [SpringFox](http://springfox.github.io/springfox/) which we've included as dependencies in ``pom.xml``

```
<properties>
    <swagger.version>2.8.0</swagger.version>
</properties>

<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>${swagger.version}</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>${swagger.version}</version>
</dependency>
```

Once started the Todo(s) API docs will be available at ``http://localhost:8080/swagger-ui.html``

<p align="center">
    <img src="https://github.com/corbtastik/todos-images/raw/master/todos-api/todos-api-swagger.png" width="640">
</p>

### Build

```bash
git clone https://github.com/corbtastik/todos-api.git
cd todos-api
./mvnw clean package
```

### Run  

```bash
java -jar target/todos-api-1.0.0.SNAP.jar
```

### Run with Overrides  

#### Set limit of 5 Todo(s)  

```bash
java -jar target/todos-api-1.0.0.SNAP.jar \
  --todos.api.limit=5
```

#### Check limit

```bash
> http :8080/todos/limit
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

5
```

### Run with Remote Debug  

```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9111,suspend=n \
  -jar target/todos-api-1.0.0.SNAP.jar
```

### Verify

Once Todo(s) API is running we can access it directly using [cURL](https://curl.haxx.se/) or [HTTPie](https://httpie.org/) to perform CRUD operations on the API.

#### Create a Todo

```bash
> http :8080/todos/ title="make bacon pancakes"
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

{
    "completed": false,
    "id": 0,
    "title": "make bacon pancakes"
}
```

#### Retrieve one Todo

```bash
> http :8080/todos/0  
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

{
    "completed": false,
    "id": 0,
    "title": "make bacon pancakes"
}
```

#### Retrieve all Todo(s)

```bash
> http :8080/todos/  
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

[
    {
        "completed": false,
        "id": 0,
        "title": "make bacon pancakes"
    },
    {
        "completed": false,
        "id": 1,
        "title": "eat bacon pancakes"
    },
    {
        "completed": false,
        "id": 2,
        "title": "eat more bacon pancakes with butter"
    }
]
```

#### Update a Todo

```bash
> http PATCH :8080/todos/0 completed="true"
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

{
    "completed": true,
    "id": 0,
    "title": "make bacon pancakes"
}
```

#### Delete one Todo

```bash
> http DELETE :8080/todos/0  
HTTP/1.1 200  
```

#### Delete all Todo(s)

```bash
> http DELETE :8080/todos/  
HTTP/1.1 200  

> http :8080/todos/
HTTP/1.1 200  
Content-Type: application/json;charset=UTF-8

[]
```

### Spring Cloud Ready

Like every Microservice in [Todos-EcoSystem](https://github.com/corbtastik/todos-ecosystem) the Todo(s) API plugs into the Spring Cloud stack several ways.

#### 1) Spring Cloud Config Client : Pull config from Config Server

From a Spring Cloud perspective we need ``bootstrap.yml`` added so we can configure several important properties that will connect this Microservice to Spring Cloud Config Server so that all external config can be pulled and applied.  We also define ``spring.application.name`` which is the default ``serviceId|VIP`` used by Spring Cloud to refer to this Microservice at runtime.  When the App boots Spring Boot will load ``bootstrap.yml`` before ``application.yml|.properties`` to hook Config Server.  Which means we need to provide where our Config Server resides.  By default Spring Cloud Config Clients (*such as Todo(s) API*) will look for Config Server on ``localhost:8888`` but if we push to the cloud we'll need to override the value for ``spring.cloud.config.uri``.

```yml
spring:
  application:
    name: todos-api
  cloud:
    config:
      uri: ${SPRING_CLOUD_CONFIG_URI:http://localhost:8888}
```

#### 2) Spring Cloud Eureka Client : Participate in service discovery

To have the Todo(s) API participate in Service Discovery we added the eureka-client dependency in our ``pom.xml``.

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
```

This library will be on the classpath and when Spring Boot starts it will automatically register with Eureka.  When running locally with Eureka we don't need to provide config to find the Eureka Server.  However when we push to the cloud we'll need to locate Eureka and that's done with the following config in ``application.yml|properties`` 

```yml
eureka:
    client:
        service-url:
            defaultZone: http://localhost:8761/eureka 
```

The ``defaultZone`` is the fallback/default zone used by this Eureka Client, however we could register with another zone under ``service-url``.

To **disable** Service Registration we can set ``eureka.client.enabled=false``.

#### 3) Spring Cloud Sleuth : Support for request tracing

Tracing request/response(s) in Microservices is no small task.  Thankfully [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth) provides easy entry into distributed tracing.  We added this dependency in ``pom.xml`` to auto-configure request tracing.

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>
```

Once added our Todo(s) API will add tracing information to each logged event.  For example:

```shell
INFO [todos-api,4542eb97e16e2cdf,8752eb97e16e2cdf,false] 36223 --- [nio-9999-exec-1] o.s.c.n.zuul.web.ZuulHandlerMapping ...
```

The event format is: ``[app, traceId, spanId, isExportable]``, where

* **app**: is the ``spring.application.name`` that sourced the log event
* **traceId**: The ID of the trace graph that contains the span
* **spanId**: The ID of a specific operation that took place
* **isExportable**: Whether the log should be exported to Zipkin

Reference the [Spring Cloud Sleuth](https://cloud.spring.io/spring-cloud-sleuth/) docs for more information.

### Verify Spring Cloud

Todo(s) API participates in Service Discovery and pulls configuration from central config Server in an environment that contains Eureka and Config Server.  Todo(s) API defines ``spring.application.name=todos-api`` in ``bootstrap.yml`` along with the location of Config Server.  Recall by default ``spring.application.name`` is used as the serviceId (or VIP) in Spring Cloud, which means we can reference Microservices by VIP in Spring Cloud.

When the API starts it will register with Eureka and other Eureka Clients such as the [Todo(s) Gateway](https://github.com/corbtastik/todos-gateway) will get a download from Eureka containing a new entry for VIP ``todos-api``.  For example if we query the Todo(s) Gateway for routes will see a new entry for Todo(s) API.  Once the route is loaded we can interact with Todo(s) API through the Gateway.

#### Query Gateway for routes

```bash
> http :9999/ops/routes
HTTP/1.1 200 
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Sun, 24 Jun 2018 01:32:35 GMT
Transfer-Encoding: chunked

{
    "/**": "http://localhost:4040",
    "/api/**": "http://localhost:8080/todos",
    "/config-server/**": "config-server",
    "/ops/**": "forward:/ops",
    "/todos-api/**": "todos-api"
}
```

#### Calling Todo(s) API through Gateway (2 ways)

```bash
> http :9999/todos-api/todos/
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8

[]

> http :9999/api/            
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8

[]
```

### Query Eureka for App Info

As mentioned when this Microservice starts it will register with Eureka, which means we could call the Eureka API directly and get information about Todo(s) API.  Eureka has an API that can be used to interact with service registry in a language neutral manner.  To get information about Todo(s) API we could make a call like so to Eureka.

```bash
# GET /eureka/apps/${vip}
http :8761/eureka/apps/todos-api
```

Which returns XML info for VIP ``todos-api``.  The complete Eureka API reference is [here](https://github.com/Netflix/eureka/wiki/Eureka-REST-operations).

```xml
<application>
    <name>TODOS-API</name>
    <instance>
        <instanceId>172.20.10.2:todos-api:8080</instanceId>
        <hostName>172.20.10.2</hostName>
        <app>TODOS-API</app>
        <ipAddr>172.20.10.2</ipAddr>
        <status>UP</status>
        <port enabled="true">8080</port>
        <securePort enabled="false">443</securePort>
        <countryId>1</countryId>
        <dataCenterInfo>...</dataCenterInfo>
        <leaseInfo>
            <renewalIntervalInSecs>30</renewalIntervalInSecs>
            <durationInSecs>90</durationInSecs>
            <registrationTimestamp>1529245795268</registrationTimestamp>
            <lastRenewalTimestamp>1529248225805</lastRenewalTimestamp>
            <evictionTimestamp>0</evictionTimestamp>
            <serviceUpTimestamp>1529245794760</serviceUpTimestamp>
        </leaseInfo>
        <metadata>
            <management.port>8080</management.port>
        </metadata>
        <homePageUrl>http://172.20.10.2:8080/</homePageUrl>
        <statusPageUrl>http://172.20.10.2:8080/ops/info</statusPageUrl>
        <healthCheckUrl>http://172.20.10.2:8080/ops/health</healthCheckUrl>
        <vipAddress>todos-api</vipAddress>
        <secureVipAddress>todos-api</secureVipAddress>
        <lastUpdatedTimestamp>1529245795268</lastUpdatedTimestamp>
        <lastDirtyTimestamp>1529245794711</lastDirtyTimestamp>
        <actionType>ADDED</actionType>
    </instance>
</application>
```

### Spring Cloud Config Client

We included ``spring-cloud-starter-config`` so Todo(s) API can pull config from Config Server.  What Config Server?  The one configured in ``bootstrap.yml`` and by default it's ``localhost:8888``.  If we look at the logs on start-up we'll see Todo(s) API reaching out to Config Server to pull down config.  For example we see "Fetching from config server" and the actual backing Property Source as a git repo (config-repo).

```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.2.RELEASE)

INFO [todos-api,,,] 7881 --- [main] c.c.c.ConfigServicePropertySourceLocator : Fetching config from server at: http://localhost:8888
INFO [todos-api,,,] 7881 --- [main] c.c.c.ConfigServicePropertySourceLocator : Located environment: name=todos-api, profiles=[default], label=null, version=e9b0ca28af40e1a7fb5adff5f502e3641cee8524, state=null
INFO [todos-api,,,] 7881 --- [main] b.c.PropertySourceBootstrapConfiguration : Located property source: CompositePropertySource {name='configService', propertySources=[MapPropertySource {name='configClient'}, MapPropertySource {name='https://github.com/corbtastik/config-repo/todos-api.properties'}]}
```

Recall we limit the number of Todo(s) that can be cached in this Microservice.  If we want to increase the limit we can override ``todos.api.limit`` on the command line or we can override in Config Server and have it inject the new value.

#### Todo(s) Config Client

Check the limit endpoint and verify its increased to 50 from 25.  We can also query the Config Server API directly.  By default with no profile(s) set the Todo(s) API Microservice will pull config from ``http://localhost:8888/${serviceId}/{profile}``, so in our case it's ``http://localhost:8888/todos-api/default``, which is where we get the new limit of 50 from.  See [Config Server](https://github.com/corbtastik/config-server) project for detailed information about [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-config/).

```bash
> http :8080/todos/limit
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Date: Sun, 24 Jun 2018 01:41:17 GMT
Transfer-Encoding: chunked

50
```

### Run on PAS

[Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) is a modern runtime for Java, .NET, Node.js apps and many more, that provides a connected 5-star development to delivery experience.  PAS provides a cloud agnostic surface for delivering apps, apps such as Spring Boot Microservices.  Rarely in computing do we see this level of harmony between an application development framework and a platform.  Its supersonic dev to delivery with only Cloud Native principles as the interface.

#### manifest.yml & vars.yml

The only PAS specific artifacts in this code repo are ``manifest.yml`` and ``vars.yml``.  Modify ``vars.yml`` to add properties **specific to your PAS environment**. See [Variable Substitution](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html#multi-manifests) for more information.  The gist is we only need to set values for our PAS deployment in ``vars.yml`` and pass that file to ``cf push``.

The Todo(s) API requires 2 environment variables:

1. ``EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE`` - Service Discovery URL
2. ``TODOS_API_LIMIT`` - How many Todo(s) your account can have

#### manifest.yml

```yml
---
applications:
- name: ((app.name))
  memory: ((app.memory))
  routes:
  - route: ((app.route))
  path: ((app.artifact))
  buildpack: java_buildpack
  env:
    ((env-key-1)): ((env-val-1))
    ((env-key-2)): ((env-val-2))
```  

#### vars.yml

```yml
app:
  name: todos-api
  artifact: target/todos-api-1.0.0.SNAP.jar
  memory: 1G
  route: todos-api.cfapps.io
env-key-1: EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE
env-val-1: http://cloud-index.cfapps.io/eureka/
env-key-2: TODOS_API_LIMIT
env-val-2: 5
```

# cf push...awe yeah

Yes you can go from zero to hero with one command :)

Make sure you're in the Todo(s) API project root (folder with ``manifest.yml``) and cf push...awe yeah!

```bash
> cf push --vars-file ./vars.yml
```

```bash
> cf app todos-api
Showing health and status for app todos-api in org bubbles / space dev as ...  

name:              todos-api
requested state:   started
instances:         1/1
usage:             1G x 1 instances
routes:            todos-api.cfapps.io
last uploaded:     Sat 23 Jun 21:33:25 CDT 2018
stack:             cflinuxfs2
buildpack:         java_buildpack

     state     since                  cpu     memory         disk           details
#0   running   2018-06-24T02:34:44Z   14.7%   379.2M of 1G   170.3M of 1G
```  

### Verify on Cloud   

Once Todo(s) API is running, use an HTTP Client such as [cURL](https://curl.haxx.se/) or [HTTPie](https://httpie.org/) and call ``/ops/info`` to make sure the app has versioning.

```bash
> http todos-api.cfapps.io/ops/info
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
X-Vcap-Request-Id: 4da5df96-5cdb-4b49-5ade-e5e9df3eaca4

{
    "build": {
        "artifact": "todos-api",
        "group": "io.corbs",
        "name": "todos-api",
        "time": "2018-06-24T02:30:55.582Z",
        "version": "1.0.0.SNAP"
    }
}
```  

#### Create a cloudy Todo

```bash
> http todos-api.cfapps.io/todos/ title="make cloudy with meatballs bacon pancakes"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Vcap-Request-Id: f48d536b-381f-40ed-6603-bc2e8dac0ac7

{
    "completed": false,
    "id": 0,
    "title": "make cloudy with meatballs bacon pancakes"
}
```

#### Retrieve one cloudy Todo

```bash
> http todos-api.cfapps.io/todos/0
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Vcap-Request-Id: 6f3c30bd-ab2b-4fd2-61a8-bd0bbe9a585c

{
    "completed": false,
    "id": 0,
    "title": "make cloudy with meatballs bacon pancakes"
}
```

#### Retrieve all cloudy Todo(s)

```bash
> http todos-api.cfapps.io/todos/  
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Vcap-Request-Id: 023728b8-b8a9-4a9f-42d0-797fc87abe99

[
    {
        "completed": false,
        "id": 0,
        "title": "make cloudy with meatballs bacon pancakes"
    },
    {
        "completed": false,
        "id": 1,
        "title": "eat cloudy with meatballs bacon pancakes...add sprinkles"
    },
    {
        "completed": false,
        "id": 2,
        "title": "eat cloudy with meatballs fried bacon pancakes...add butter"
    }
]
```

#### Update a cloudy Todo

```bash
> http PATCH todos-api.cfapps.io/todos/0 completed=true  
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Vcap-Request-Id: 0f9ca41b-3c3f-43b0-6978-35fe4cce976b

{
    "completed": true,
    "id": 0,
    "title": "make cloudy with meatballs bacon pancakes"
}
```

#### Delete one cloudy Todo

```bash
> http DELETE todos-api.cfapps.io/todos/0  
HTTP/1.1 200 OK
X-Vcap-Request-Id: aca8097d-fea2-4e0e-62f8-b1754496f5b7

> http todos-api.cfapps.io/todos/0  
HTTP/1.1 200 OK
X-Vcap-Request-Id: 88c0ab16-8092-4aaf-79fc-27a0aad39ff3
```

#### Delete all cloudy Todo(s)  

```bash
> http DELETE todos-api.cfapps.io/todos/  
HTTP/1.1 200 OK
X-Vcap-Request-Id: 4754c38e-7204-4b5b-45a3-ef68b208a764

> http todos-api.cfapps.io/todos/  
HTTP/1.1 200 OK
X-Vcap-Request-Id: 8d5dfd42-a7c7-4fd5-7cff-384e04d550fb

[]
```

### Stay Frosty  

#### Adventure Time - [take some bacon and put it in a pancake!](https://www.youtube.com/watch?v=cUYSGojUuAU)  

### References

* [Eureka in 10 mins](https://blog.asarkar.org/technical/netflix-eureka/)
