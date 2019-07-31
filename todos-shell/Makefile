BRANCH=master
# clone minus todos-shell (this project)
clone:
	pushd .. && git clone https://github.com/corbtastik/todos-api && \
		git clone https://github.com/corbtastik/todos-app && \
		git clone https://github.com/corbtastik/todos-edge && \
		git clone https://github.com/corbtastik/todos-mysql && \
		git clone https://github.com/corbtastik/todos-processor && \
		git clone https://github.com/corbtastik/todos-redis && \
		git clone https://github.com/corbtastik/todos-sink && \
		git clone https://github.com/corbtastik/todos-webui && popd

build:
	pushd ../todos-api && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-app && git checkout master && ./mvnw clean package -DskipTests && popd
	pushd ../todos-edge  && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-mysql && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-processor && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-redis && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-sink && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-webui && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd
	pushd ../todos-shell && git checkout ${BRANCH} && ./mvnw clean package -DskipTests && popd

mkdir-jars:
	mkdir -p ./.apps/jars

copy-jars: mkdir-jars
	cp ./.apps/todos-app/target/todos-app-1.0.0.SNAP.jar ./.apps/jars
	cp ./.apps/todos-redis/target/todos-redis-1.0.0.SNAP.jar ./.apps/jars
	cp ./.apps/todos-mysql/target/todos-mysql-1.0.0.SNAP.jar ./.apps/jars
	cp ./.apps/todos-webui/target/todos-webui-1.0.0.SNAP.jar ./.apps/jars
	cp ./.apps/todos-api/target/todos-api-1.0.0.SNAP.jar     ./.apps/jars
	cp ./.apps/todos-edge/target/todos-edge-1.0.0.SNAP.jar   ./.apps/jars

clean:
	rm -rf ./.apps

setup: mkdir-app clone-all build-all copy-jars
