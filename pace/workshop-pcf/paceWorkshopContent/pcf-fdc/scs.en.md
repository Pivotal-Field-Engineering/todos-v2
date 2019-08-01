### Spring Cloud for Todo sample set

This Shop is about getting familiar with [Spring Cloud Services for PCF](https://docs.pivotal.io/spring-cloud-services/common/index.html) by adding [Spring Cloud](https://spring.io/projects/spring-cloud) dependencies to the Todo(s) sample set.  Once this Shop is complete you'll have virtually the same deployment of apps with the added benefit of Spring Cloud to handle Configuration and Connectivity.

![Todos Samples with SCS](shop-3.png)

### Git Repository for application config

First things first is exploring the git-repo that will drive your config.  Browse to the `config` branch of [Todos-App git repo](https://github.com/Pivotal-Field-Engineering/todos-apps/tree/config).  

### Creating a Spring Cloud Config Service Instance

We want to control application configurations from a central place and [Spring Cloud Config server](https://docs.pivotal.io/spring-cloud-services/2-0/common/config-server/index.html) is a great way to get up and running.  First let's create a basic Spring Cloud Config Service instance and configure with your `todos-config` repository.

Use `cf create-service` to provision `YOUR` Config Service instance, passing `-c` some configuration that points to the backing git-repo.

```bash
cd ~/Desktop/todos-apps/pcf-config-server
cf create-service p-config-server standard your-todos-config \
    -c '{"git": { "uri": "https://github.com/Pivotal-Field-Engineering/todos-apps", "label": "config" } }'
# after a few moments check the status
cf service your-todos-config
> name:      your-todos-config
> service:   p-config-server
> status:    create succeeded
```

### Creating a Spring Cloud Service Registry Instance

We also want an eco-system where applications can connect with other applications and remove the burden of needing to configure URLs and client-side application access.  [Spring Cloud Service Registry](https://docs.pivotal.io/spring-cloud-services/2-0/common/service-registry/index.html) can help "connect" our apps in a Spring Cloud context.

Use `cf create-service` to provision `YOUR` Service Registry instance.

```bash
cd ~/Desktop/todos-apps/pcf-service-registry
cf create-service p-service-registry standard your-todos-registry
# after a few moments check the status
cf service your-todos-registry
> name:      your-todos-registry
> service:   p-service-registry
> status:    create succeeded
```

### Inspect cloud branch

Switch to cloud branch; you may need to stash or commit local code changes to master branch before checking out cloud.

```bash
cd ~/Desktop/todos-apps/
git checkout cloud
```

* Code and/or inspection time
    * Spring Cloud Services dependencies
    * Open Source Spring Cloud versions
    * Application configuration

### Build Spring Cloud apps

Build Spring Cloud ready versions of `todos-edge`, `todos-api` and `todos-webui`

Manual steps to build, same as before except this time we build with [spring-cloud dependencies](https://docs.pivotal.io/spring-cloud-services/2-0/common/client-dependencies.html).

Each `pom.xml` on the `cloud` branch will contain these Spring Cloud Services dependencies.  See [Including Spring Cloud Services Dependencies](https://docs.pivotal.io/spring-cloud-services/2-0/common/client-dependencies.html#including-dependencies).

```xml
    <dependencies>
        <!-- Required for SCS Config Server
             brings in Spring Cloud Config Client
             and Spring Security OAuth 2 -->
        <dependency>
            <groupId>io.pivotal.spring.cloud</groupId>
            <artifactId>
                spring-cloud-services-starter-config-client
            </artifactId>
        </dependency>
        <!-- Required for SCS Service Discovery
            brings in Spring Cloud Netflix Eureka Client,
            Jersey Client, Spring Security OAuth 2 -->
        <dependency>
            <groupId>io.pivotal.spring.cloud</groupId>
            <artifactId>
                spring-cloud-services-starter-service-registry
            </artifactId>
        </dependency>

    <!-- other dependencies -->
    </dependencies>
```

```bash
# change into your working directory (i.e. todos-apps)
cd ~/Desktop/todos-apps
./mvnw clean package
```

### Configure manifests for Spring Cloud apps

Configure manifests to bind to Spring Cloud services instances created above

Spring Cloud Services uses HTTPs for all client-to-service communication.  The `TRUST_CERTS` environment variable is applicable if your PCF environment uses Self-Signed Certificates.  Spring Cloud Services will add this Self-Signed Certificate to the JVM trust-store so Spring Cloud pushed apps can register and consume Spring Cloud Services using HTTPs.

Set `TRUST_CERTS` to your PCF api endpoint (`cf target`), if you're using Self-Signed Certificates.

Edit the manifests for `YOUR` apps, making sure the `services` configuration has `YOUR` Config Server and Service Registry instance.

#### todos-api cloud manifest

```yaml
---
applications:
- name: your-todos-api
  memory: 1G
  path: target/todos-api-1.0.0.SNAP.jar
  buildpack: java_buildpack
  services:
  - your-todos-config
  - your-todos-registry
  env:
    TRUST_CERTS: api.sys.retro.io
```

#### todos-webui cloud manifest

```yaml
---
applications:
- name: your-todos-webui
  memory: 1G
  path: target/todos-webui-1.0.0.SNAP.jar
  services:
  - your-todos-config
  - your-todos-registry
  env:
    TRUST_CERTS: api.sys.retro.io
```

#### todos-edge cloud manifest

```yaml
---
applications:
- name: todos-edge
  memory: 1G
  routes:
  - route: todos-edge.apps.retro.io
  - route: todos.apps.retro.io  
  path: target/todos-edge-1.0.0.SNAP.jar
  services:
  - todos-config
  - todos-registry
  env:
    TRUST_CERTS: api.sys.retro.io
```

### Push Spring Cloud apps to PCF

#### Push todos-api cloud

Replacing `YOUR` with your unique app tag.  **Note** - Leave the `-todos-api` suffix.

* `cf push YOUR-todos-api` - PCF will again use the Java Buildpack to containerize the application and schedule a container instance to run our Spring Cloud app.

```bash
cd ~/Desktop/todos-apps/todos-api
cf push your-todos-api
cf apps
> Getting apps in org retro / space arcade as corbs...
> OK
> name         state   instances memory disk urls
> your-todos-api started 1/1       1G     4G   your-todos-api.apps.retro.io
```

#### Push todos-webui cloud

Replacing `YOUR` with your unique app tag.  **Note** - Leave the `-todos-webui` suffix.

* `cf push YOUR-todos-webui` - PCF will again use the Java Buildpack to containerize the application and schedule a container instance to run our Spring Cloud app.

```bash
cd ~/Desktop/todos-apps/todos-webui
cf push your-todos-webui
cf apps
> Getting apps in org retro / space arcade as corbs...
> OK
> name         state   instances memory disk urls
> your-todos-webui started 1/1       1G     4G   your-todos-webui.apps.retro.io
```

#### Push todos-edge cloud

Replacing `YOUR` with your unique app tag.  **Note** - Leave the `-todos-edge` suffix.

* `cf push YOUR-todos-edge` - PCF will again use the Java Buildpack to containerize the application and schedule a container instance to run our Spring Cloud app.

```bash
cd ~/Desktop/todos-apps/todos-edge
cf push your-todos-edge
cf apps
> Getting apps in org retro / space arcade as corbs...
> OK
> name         state   instances memory disk urls
> your-todos-edge started 1/1       1G     4G   your-todos-edge.apps.retro.io
```

### Sync Point

* Review what PCF together with Spring Cloud Services did to bind backing services and for the apps to pull cloud configs and register with Service Registry.
* Make config change to todos-webui `placeholder` property to customize the UI placeholder.
* Refresh Todos WebUI
* Show updated placeholder on WebUI and walk through how the refresh works
* Next steps - refresh bus, encrypted values
* Extra mile - Use Todo Shell to automate pushing Spring Cloud Service ready apps
    * `shell:>push-scs --tag myscsapp`
