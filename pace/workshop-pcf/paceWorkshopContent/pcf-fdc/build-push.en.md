### Introduce Todo sample set (whiteboard or slide with picture)

In this Shop we're going to build a simple 3 service App that consist of a [backing API](#todos-api) implemented in Spring Boot, a [UI which is a Spring Boot vendored Vue.js app](#todos-webui) and an Edge implemented with Spring Cloud Gateway to help with application level routing.

This Shop is plain-ole Spring Boot without Spring Cloud (which is later) and relies simply on core PCF features.

At the end of this Shop you'll have 3 apps running in PCF that have been manually configured to work together.

![Shop 1](shop-1.png)

### 1. Build

This assumes you've completed [Projects Setup](#projects-setup), if not please check that box.

Manual steps to build.

```bash
# change into your working directory (i.e. todos-apps)
cd ~/Desktop/todos-apps
cd todos-api
./mvnw clean package
cd ../todos-edge
./mvnw clean package
cd ../todos-webui
./mvnw clean package
cd ..
```

A successful build puts a Spring Boot jar in each projects `target` directory, for example you should have these jars after building

* [`todos-api`](#todos-api)
* [`todos-edge`](#todos-edge)
* [`todos-webui`](#todos-webui)

```bash
cd ~/Desktop/todos-apps
# jars after build
./todos-api/target/todos-api-1.0.0.SNAP.jar
./todos-edge/target/todos-edge-1.0.0.SNAP.jar
./todos-webui/target/todos-webui-1.0.0.SNAP.jar
```

### 2. Push to PCF

Pick a unique "**tag**" and stick with it throughout the workshop, in the docs and snippets below this is represented by the text `YOUR`.

* Configure manifests for
    * `todos-api/manifest.yml`
    * `todos-webui/manifest.yml`
    * `todos-edge/manifest.yml`
* `cf domains` - to figure out which domains you fall under
    * In the snippet below `apps.retro.io` is the cf domain
    * On PWS the domain would be `cfapps.io`
* `cf buildpacks` - to figure out the exact java buildpack name, its safe to remove the `buildpack` property from the `manifest.yml` files and let PCF figure out which to use
    * This sample set uses the [Java Buildpack](https://github.com/cloudfoundry/java-buildpack)

#### todos-api manifest

```yaml
---
applications:
- name: todos-api
  memory: 1G
  path: target/todos-api-1.0.0.SNAP.jar
  buildpack: java_buildpack
  env:
    TODOS_API_LIMIT: 1024
```

#### Push todos-api to PCF

Replacing `YOUR` with your unique app tag.  **Note** - Leave the `-todos-api` suffix.

* `cf push YOUR-todos-api`

```bash
cd ~/Desktop/todos-apps/todos-api
# edit todos-api/manifest.yml to your liking or simply push
cf push your-todos-api
# pcf will use the java buildpack to create a container image
# and start that image up as a container instance with networking
cf apps
> Getting apps in org retro / space arcade as corbs...
> OK
> name         state   instances memory disk urls
> your-todos-api started 1/1       1G     4G   your-todos-api.apps.retro.io
```

* `cf logs YOUR-todos-api --recent` - take a gander at the startup logs

#### todos-webui manifest

Customize the UI text entry placeholder if you wish.

```yaml
---
applications:
- name: todos-webui
  memory: 1G
  path: target/todos-webui-1.0.0.SNAP.jar
  buildpack: java_buildpack
  env:
    TODOS_UI_PLACEHOLDER: 'Time to wash the dog?'
```

#### Push todos-webui to PCF

Replacing `YOUR` with your unique app tag.  **Note** - Leave the `-todos-webui` suffix.

* `cf push YOUR-todos-webui`

```bash
cd ~/Desktop/todos-apps/todos-webui
# edit todos-webui/manifest.yml to your liking or simply push
cf push your-todos-webui
# pcf will use the java buildpack to create a container image
# and start that image up as a container instance with networking
cf apps
> Getting apps in org retro / space arcade as corbs...
> OK
> name             state   instances memory disk urls
> your-todos-webui started 1/1       1G     4G   your-todos-webui.apps.retro.io
```

* Open your todos-webui url in a browser ([Chrome recommended](https://www.google.com/chrome/)) and notice the failed resource error.  The UI isn't connected to anything at this point.

![Todo(s) WebUI endpoint](todos-webui-endpoint.png)

#### todos-edge manifest

The critical configuration here is getting the routes entered for the API and UI apps.

```bash
---
applications:
- name: YOUR-todos-edge
  memory: 1G
  path: target/todos-edge-1.0.0.SNAP.jar
  env:
  # add YOUR endpoints
    TODOS_UI_ENDPOINT: https://YOUR-todos-webui.apps.retro.io
    TODOS_API_ENDPOINT: https://YOUR-todos-api.apps.retro.io
```

#### Push todos-edge to PCF

```bash
cd ~/Desktop/todos-apps/todos-edge
# edit todos-edge/manifest.yml with your API and UI endpoints
cf push your-todos-edge
# pcf will use the java buildpack to create a container image
# and start that image up as a container instance with networking
cf apps
> Getting apps in org retro / space arcade as corbs...
> OK
> name            state   instances memory disk urls
> your-todos-edge started 1/1       1G     4G   your-todos-edge.apps.retro.io
```

* Open `todos-edge` in Chrome
    * You'll have a route to your `todos-edge` app...for example `https://your-todos-edge.apps.retro.io`

![Todo(s) Edge endpoint](todos-edge-endpoint.png)

* Create, Read, Update and Delete Todo(s) from the UI and verify all is well
* Slap a high-five or something as you've manually completed pushing the app

### Extra Mile

* Create a custom route in cf and map to your `todos-edge` to `SOMETHING`
    * `cf map-route your-todos-edge apps.retro.io --hostname SOMETHING`
* Use [Todo(s) Shell](#todos-shell) to automate the deployment of the same three apps as a single functioning "Todo app" on PCF with one command.  The shell will use your PCF creds and push configured apps to the platform.  The deploy results in 3 apps running (`todos-edge`,`todos-api`,`todos-webui`) each with a user provided "tag" which will prefix the running instances.
    * `shell:>push-app --tag corbs`

Pause...take a quick review and field questions

### Talking Points

* Intro to Spring Boot and [Sample Set](#shop-sample-set)
* Introduce Spring Cloud Gateway as an application edge and routing tool
* Code or inspect [Todo Edge](#todos-edge), [Todos API](#todos-api) and [Todos WebUI](#todos-webui) locally, inspect both master and cloud branches for differences, note cloud branch uses Spring Cloud semantics for connectivity so we can stop maintaining URIs and such.  
* Introduce WebUI and simply show it's a Spring Boot app vendoring a frontend Javascript/HTML/CSS app.
