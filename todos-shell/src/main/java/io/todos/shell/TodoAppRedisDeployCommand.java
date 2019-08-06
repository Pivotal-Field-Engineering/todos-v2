package io.todos.shell;

import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TodoAppRedisDeployCommand {

    private final CfClient cfClient;

    @Autowired
    public TodoAppRedisDeployCommand(CfClient cfClient) {
        this.cfClient = cfClient;
    }

    public void push(String tag, String version, String serviceInstance) {

        // push redis backend app
        cfClient.pushApplication(tag + "-todos-redis", "todos-redis-" + version + ".jar")
            .then(cfClient.ops().services().bind(BindServiceInstanceRequest.builder()
                .applicationName(tag + "-todos-redis")
                .serviceInstanceName(serviceInstance)
                .build()))
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-redis").build())).subscribe();

        // push webui
        cfClient.pushApplication(tag + "-todos-webui", "todos-webui-" + version + ".jar")
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-webui").build())).subscribe();

        // push edge and manually config UI and API endpoints in edge's ENV
        cfClient.pushApplication(tag + "-todos-edge", "todos-edge-" + version + ".jar")
            .then(cfClient.ops().applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                    .name(tag + "-todos-edge")
                    .variableName("TODOS_UI_ENDPOINT")
                    .variableValue("https://" + tag + "-todos-webui." + cfClient.domain())
                    .build()))
            .then(cfClient.ops().applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                    .name(tag + "-todos-edge")
                    .variableName("TODOS_API_ENDPOINT")
                    .variableValue("https://" + tag + "-todos-redis." + cfClient.domain())
                    .build()))
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-edge").build())).subscribe();
    }
}
