TODOS_HOME=~/dev/github/pivotal/todos-apps
TODOS_VERSION=1.0.0.SNAP
TODOS_JARS=./.apps/jars

mkdir-jars:
	mkdir -p ${TODOS_JARS}

copy-jars: mkdir-jars
	cp ${TODOS_HOME}/todos-api/target/todos-api-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-edge/target/todos-edge-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-loggregator/target/todos-loggregator-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-mysql/target/todos-mysql-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-processor/target/todos-processor-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-redis/target/todos-redis-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-restclient/target/todos-restclient-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-sink/target/todos-sink-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-source/target/todos-source-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-webclient/target/todos-webclient-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-webflux/target/todos-webflux-${TODOS_VERSION}.jar ./.apps/jars
	cp ${TODOS_HOME}/todos-webui/target/todos-webui-${TODOS_VERSION}.jar ./.apps/jars

clean:
	rm -rf ${TODOS_JARS}