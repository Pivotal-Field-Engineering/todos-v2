# Todo(s) Loggregator

The Todo(s) Loggregator is a sample [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) [source](https://cloud.spring.io/spring-cloud-stream/spring-cloud-stream.html#_programming_model) application that sources Container Metrics, Log and Error Messages for applications running on PCF.

It works by using the [cf-java-client](https://github.com/cloudfoundry/cf-java-client) to pull application events from the [PCF Loggregator](https://docs.pivotal.io/pivotalcf/2-6/loggregator/architecture.html#components).

## Default Configuration

Like all Spring Boot apps the default application configuration is in `application.yml`.  The salient properties for this sample are the broker endpoints for rabbitmq or kafka and the publishing destinations.

```yaml
spring:
    cloud:
        stream:
            # kafka: needed if built with -P kafka option
            #     binder:
            #         brokers: 'kafka.retro.io'
            bindings:
                logEvents:
                    destination: todos-logs
                errorEvents:
                    destination: todos-errors
                containerMetricEvents:
                    destination: todos-container-metrics
cf:
    api: api.sys.retro.io     # change to your cf api
    username: corbs           # your pcf username
    password: changeme        # your pcf password
    organization: retro       # your pcf organization
    space: arcade             # your pcf space
    domain: apps.retro.io     # your default pcf domain
    skipSslValidation: true   # skip ssl validation if using self-signed certs
```

## Build

This project is a standard Spring Boot application created from [start.spring.io](https://start.spring.io) and relies on the following core libraries.

* `spring-boot-starter-webflux` - event stream start/stop API
* `spring-cloud-streams` - stream binding
* `cf-java-client` - integration with PCF logging system

```bash
# build with default rabbitmq binder
./mvnw clean package
# build with kafka binder
./mvnw clean package -P kafka
```

## Run

This app is intended to run on PCF and requires access to RabbitMQ or Kafka so application events can be published for consumption.

Depending on how you built (`rabbitmq|kafka`) will determine how you connect to the underlying messaging middleware.

### With RabbitMQ for PCF

On PCF with RabbitMQ services its simple enough to build and push+bind to a deployed RabbitMQ Service Instance.  For example...

Create a RabbitMQ Service instance.

```bash
cf create-service p.rabbitmq mq-small messaging-service
```

Bind to the RabbitMQ Service Instance and cf push...awe yeah.

```yaml
# manifest-rabbitmq.yml
---
applications:
- name: todos-loggregator
  memory: 1G
  path: target/todos-loggregator-1.0.0.SNAP.jar
  services:
  - messaging-service
```

```bash
cf push -f manifest-rabbitmq.yml
```

### With Kafka

If using Kafka make sure you build with `./mvnw clean package -P kafka` and provide the right configuration for the Kafka broker.  Since we're directly configuring the broker we opt out on [Spring Auto Reconfiguration](https://github.com/cloudfoundry/java-buildpack-auto-reconfiguration) provided by the Java Buildpack.  This [disables rewriting of beans](https://github.com/cloudfoundry/java-buildpack-auto-reconfiguration) for PCF backing services since we're not using them for brokering Kafka connectivity.

```yaml
---
applications:
- name: todos-loggregator
  memory: 1G
  path: target/todos-loggregator-1.0.0.SNAP.jar
  buildpack: java_buildpack
  env:
    SPRING_PROFILES_ACTIVE: kafka
    JBP_CONFIG_SPRING_AUTO_RECONFIGURATION: '{ enabled: false }'
```

```bash
cf push -f manifest-kafka.yml
```

## Use

The application exposes 2 control methods, `start` and `stop`.  Both are `PUT` operations and take an `applicationId` which is the UUID of the PCF application to stream events for.

When `todos-loggregator` receives a request to start streaming it opens a Reactive Stream to the PCF Doppler system and subscribes to events for the given applicationId.  The Reactive Stream reference is maintained in a local context, which can be used to dispose of the Reactive Stream at a later point in time.  For example by calling `stop`.

Be mindful how many applications you stream logs with, this is merely a sample and the code does not stop you from streaming out many applications.  All that to say the more applications you start stream for the more **chatty** this application will become.

The primary function of this application is to source 3 different topics with events.

Raw data from Doppler gets converted to 1 of 3 types of events which can be consumed by downstream applications.

1. Contianer Metrics (`destination=todos-container-metrics`)
1. Log Event Messages (`destination=todos-logs`)
1. Error Messages (`destination=todos-errors`)

### Starting and Stopping application stream sourcing

Make a `PUT` request to the `start` endpoint and pass a valid applicationId to start output streaming.

```bash
http PUT todos-loggregator.apps.retro.io/start/40d7b8d6-6f89-49c0-bd34-c137cdd6c0c1
```

Once started the output destinations defined in `application.yml` will receive targeted events (Container Metric, Log or Error).  This should be evident by monitoring the topic activity in Kafka (Confluent Control Center is an easy option).

<img src="../todos-docs/docs/todos-workshop/img/todos-loggregator-kafka.png" width="70%">

Make a `PUT` request to the `stop` endpoint to dispose of the stream source.

```bash
http PUT todos-loggregator.apps.retro.io/stop/40d7b8d6-6f89-49c0-bd34-c137cdd6c0c1
```
