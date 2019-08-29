all:
	./mvnw clean package
base:
	./mvnw -pl todos-api clean package
	./mvnw -pl todos-webflux clean package
	./mvnw -pl todos-edge clean package
	./mvnw -pl todos-webui clean package
streams:
	./mvnw -pl todos-source clean package
	./mvnw -pl todos-sink clean package
	./mvnw -pl todos-processor clean package
	./mvnw -pl todos-loggregator clean package
streams-kafka:
	./mvnw -pl todos-source clean package -P kafka
	./mvnw -pl todos-sink clean package -P kafka
	./mvnw -pl todos-processor clean package -P kafka
	./mvnw -pl todos-loggregator clean package -P kafka
upload-streams:
	pushd todos-source/target && uploadjar.sh todos-source-1.0.0.SNAP.jar io/todos/todos-source/1.0.0.SNAP/todos-source-1.0.0.SNAP.jar && popd
	pushd todos-processor/target && uploadjar.sh todos-processor-1.0.0.SNAP.jar io/todos/todos-processor/1.0.0.SNAP/todos-processor-1.0.0.SNAP.jar && popd
	pushd todos-sink/target && uploadjar.sh todos-sink-1.0.0.SNAP.jar io/todos/todos-sink/1.0.0.SNAP/todos-sink-1.0.0.SNAP.jar && popd
	pushd todos-loggregator/target && uploadjar.sh todos-loggregator-1.0.0.SNAP.jar io/todos/todos-loggregator/1.0.0.SNAP/todos-loggregator