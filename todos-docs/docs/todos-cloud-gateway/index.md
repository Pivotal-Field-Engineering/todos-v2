# README

## Todo(s) Cloud Gateway

Todo(s) Cloud Gateway is a simple edge service that routes to the proper Todo application.  It's implemented in Kotlin (but has very little code) using the ``spring-cloud-starter-gateway`` dependency.

### Run on Localhost

The default configuration starts a server on port ``9999`` and it configured to provide edge-service for Todo(s) UI and API.  That can be seen in ``application.yml`` and can be customized by overriding those properties on boot.

```yml
todos:
  api:
    endpoint: http://localhost:8080
  ui:
    endpoint: http://localhost:4040
```

By default all paths that start with ``/todos/**`` will be routed to Todo(s) API and root path ``/**`` is the last predicate to apply and will route to Todo(s) UI.

### Run on PAS (cloud)

The manifest for Todo(s) Cloud Gateway has placeholders for ``((ui.route))`` and ``((api.route))``. those values can be set in a ``vars.yml`` file or passed as individual var-key pairs when you ``cf push``.

#### manifest

```yml
- name: todos-cloud-gateway
  memory: 1G
  routes:
  - route: ((gateway.route))
  - route: ((app.route))  
  path: ((gateway.artifact))
  buildpack: java_buildpack
  env:
    TODOS_UI_ENDPOINT: ((ui.route))
    TODOS_API_ENDPOINT: ((api.route))
    EUREKA_CLIENT_ENABLED: false
```

### vars

```yml
api:
  route: https://todos-api.cfapps.io
  artifact: todos-api/target/todos-api-1.0.0.SNAP.jar
ui:
  route: https://todos-ui.cfapps.io
  artifact: todos-ui/
```

#### cf push

```bash
$ ./mvnw clean package
$ cf push --vars-file ./vars.yml
# or
$ cf push --vars-file ./vars.yml --var api.route=https://todos-api.cfapps.io

```