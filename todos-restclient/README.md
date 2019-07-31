## Todo(s) Rest Client

Howdy and welcome.  This repository contains a Microservice implemented in Spring Boot and highlighting use of RestTemplate as a client to call APIs.  This Microservice forwards calls to a backing API using RestTemplate, its part of a larger [Todo(s) EcoSystem](https://github.com/corbtastik/todos-ecosystem) demo set of apps each highlighting a unique aspect of Spring Boot, Spring Cloud and Pivotal Application Service.

### API operations

1. **C**reate a Todo
2. **R**etrieve one or more Todo(s)
3. **U**pdate one Todo
4. **D**elete one Todo

### Build

```bash
git clone https://github.com/corbtastik/todos-restclient.git
cd todos-restclient
./mvnw clean package
```

### Run 

```bash
# starts on 8006 by default
java -jar target/todos-restclient-1.0.0.SNAP.jar
```

### Run with Remote Debug 
```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9111,suspend=n \
  -jar target/todos-restclient-1.0.0.SNAP.jar
```

### Verify

Once Todo(s) Rest Client is running we can access it directly using [cURL](https://curl.haxx.se/) or [HTTPie](https://httpie.org/) to perform operations on the API.

#### Create a Random Todo

```bash
> http POST :8006/
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Date: Mon, 18 Jun 2018 19:53:51 GMT
Transfer-Encoding: chunked

{
    "completed": false,
    "id": 4,
    "title": "todo from restclient"
}
```

#### Retrieve one Todo

```bash
> http :8006/4    
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Date: Mon, 18 Jun 2018 19:54:04 GMT
Transfer-Encoding: chunked

{
    "completed": false,
    "id": 4,
    "title": "todo from restclient"
}
```

#### Retrieve all Todo(s)

```bash
> http :8006/ 
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Date: Mon, 18 Jun 2018 19:55:37 GMT
Transfer-Encoding: chunked

[
    {
        "completed": false,
        "id": 3,
        "title": "commit readme"
    },
    {
        "completed": false,
        "id": 4,
        "title": "todo from restclient"
    }
]

```

### Spring Cloud Ready

Like every Microservice in Todos-EcoSystem the Todo(s) RestClient plugs into the Spring Cloud stack several ways.

#### 1) Spring Cloud Config Client : Pull config from Config Server

From a Spring Cloud perspective we need ``bootstrap.yml`` added so we can configure several important properties that will connect this Microservice to Spring Cloud Config Server so that all external config can be pulled and applied.  We also define ``spring.application.name`` which is the default ``serviceId|VIP`` used by Spring Cloud to refer to this Microservice at runtime.  When the App boots Spring Boot will load ``bootstrap.yml`` before ``application.yml|.properties`` to hook Config Server.  Which means we need to provide where our Config Server resides.  By default Spring Cloud Config Clients (*such as Todo(s) API*) will look for Config Server on ``localhost:8888`` but if we push to the cloud we'll need to override the value for ``spring.cloud.config.uri``.

```yml
spring:
  application:
    name: todos-restclient
  cloud:
    config:
      uri: ${SPRING_CLOUD_CONFIG_URI:http://localhost:8888}
```

#### 2) Spring Cloud Eureka Client : Participate in service discovery

To have the Todo(s) RestClient participate in Service Discovery we added the eureka-client dependency in our pom.xml.

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
            defaultZone: ${SPRING_CLOUD_SERVICE_DISCOVERY:http://localhost:8761/eureka}
```

The ``defaultZone`` is the fallback/default zone used by this Eureka Client, we could register with another zone should one be created in Eureka.

To **disable** Service Registration we can set ``eureka.client.enabled=false``.

#### 3) Spring Cloud Sleuth : Support for request tracing

Tracing request/response(s) in Microservices is no small task.  Thankfully Spring Cloud Sleuth provides easy entry into distributed tracing.  We added this dependency in ``pom.xml`` to auto-configure request tracing.

```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>
```

Once added our Todo(s) RestClient will add tracing information to each logged event.  For example:

```shell
INFO [todos-restclient,4542eb97e16e2cdf,8752eb97e16e2cdf,false] 36223 --- [nio-9999-exec-1] o.s.c.n.zuul.web.ZuulHandlerMapping ...
```

The event format is: ``[app, traceId, spanId, isExportable]``, where

* **app**: is the ``spring.application.name`` that sourced the log event
* **traceId**: The ID of the trace graph that contains the span
* **spanId**: The ID of a specific operation that took place
* **isExportable**: Whether the log should be exported to Zipkin

Reference the [Spring Cloud Sleuth](https://cloud.spring.io/spring-cloud-sleuth/) docs for more information.

### Run on PAS

#### cf push...awe yeah

Yes you can go from zero to hero with one command :)

Make sure you're in the Todo(s) RestClient project root (folder with ``manifest.yml``) and cf push...awe yeah!

```bash
> cf push --vars-file ./vars.yml
```

```bash
> cf app todos-restclient  
Showing health and status for app todos-restclient in org bubbles / space dev as ...

name:              todos-restclient
requested state:   started
instances:         1/1
usage:             1G x 1 instances
routes:            todos-restclient.cfapps.io
last uploaded:     Tue 26 Jun 14:04:04 CDT 2018
stack:             cflinuxfs2
buildpack:         java_buildpack

     state     since                  cpu    memory         disk           details
#0   running   2018-06-26T19:05:18Z   0.3%   369.1M of 1G   165.5M of 1G
```  

### Verify on Cloud  

Once Todo(s) RestClient is running, use an HTTP Client such as [cURL](https://curl.haxx.se/) or [HTTPie](https://httpie.org/) and call ``/ops/info`` to make sure the app has versioning.

```bash
> http todos-restclient.cfapps.io/ops/info
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
X-Vcap-Request-Id: 059abe96-5825-401b-7ab2-fc697fb5f15a

{
    "build": {
        "artifact": "todos-restclient",
        "group": "io.corbs",
        "name": "todos-restclient",
        "time": "2018-06-26T19:01:58.797Z",
        "version": "1.0.0.SNAP"
    }
}
```  
