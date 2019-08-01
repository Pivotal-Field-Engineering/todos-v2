# Todo(s) Redis

Howdy and welcome...you can never have enough quick cache so to speak.  Inevitably you'll need to implement Microservices to cache data for many purposes.  There's several common cache access patterns such as Cache-Aside, Read-Through, Write-Through and Write-Behind.  Another pattern is Caching as a System of Record where Microservices fuse onto a distributed cache (such as a grid like Gemfire or Pivotal Cloud Cache).

## Primary dependencies

* Spring Boot Starter Web (Embedded Tomcat)
* Spring Boot Starter Redis
* Spring Boot Actuators (actuator endpoints)
* Spring Data Rest (blanket our Redis model in a REST API)

### Build

```bash
git clone https://github.com/Pivotal-Field-Engineering/todos-apps.git
cd todos-apps
# to build only the todos-redis module
./mvnw -pl todos-redis clean package
```

### Run on PCF

Configure your `manifest.yml` to meet your needs, it's simple enough to create a Redis Service Instance and bind to it on push like so...

Create the on-demand Redis Service Instance.

```bash
cf create-service p.redis cache-small todos-redis
```

Configure your `manifest.yml`, declaring a binding to the Redis Service Instance.

```yaml
---
applications:
- name: todos-redis
  memory: 1G
  path: target/todos-redis-1.0.0.SNAP.jar
  services:
   - todos-redis
```

```bash
cd todos-apps/todos-redis
cf push -f manifest.yml # aweeee yeah
```

### Verify

```bash
http todos-redis.apps.retro.io/
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Vcap-Request-Id: 732a9cfb-b910-4645-4544-fd6902e1bd71

[]
```

### References  

See this issue when deploying to cf
https://github.com/spring-guides/gs-messaging-redis/issues/3