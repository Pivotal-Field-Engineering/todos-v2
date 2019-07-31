# Todos Spring Cloud Stream example

Howdy!  This is a small [Spring Cloud Streams](https://cloud.spring.io/spring-cloud-stream/) example that handles Todo events by building a Hashtag index.  It requires running the [Source](https://github.com/corbtastik/todos-source) and [Processor](https://github.com/corbtastik/todos-processor) as well as RabbitMQ.

## Use

### Clone 3 Stream apps

* [Todos Source](https://github.com/corbtastik/todos-source) - event Todos
* [Todos Processor](https://github.com/corbtastik/todos-processor) - process Todos
* [Todos Sink](https://github.com/corbtastik/todos-sink) - handle Todos

### Build each app

``mvnw clean package``

### Start each app

```bash
java -jar ./target/todos-source-1.0.0-SNAP.jar --server.port=8080
java -jar ./target/todos-processor-1.0.0-SNAP.jar --server.port=8081
java -jar ./target/todos-sink-1.0.0-SNAP.jar --server.port=8082
```

### Invoke Stream

POST a todo to the todos-source endpoint, for example using [HttpPie](https://httpie.org/) to make the call...

```bash
http :8080/ title="#one #two three"
```

### Observe

If a Todo has a hashtag the Processor adds it into a Set and republishes the event which in-turn is handled by the Sink and added into a Hashtag Index.

You can see evidence of this by tailing the log of the Sink while you Source a Todo with a hashtag in the title.
