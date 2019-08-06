package io.todos.shell;


import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TodoAppDeployCommand {

    private final CfClient cfClient;

    @Autowired
    public TodoAppDeployCommand(CfClient cfClient) {
        this.cfClient = cfClient;
    }

    public void push(String tag, String version) {

        if (tag.length() < 1) {
            tag = UUID.randomUUID().toString().substring(0, 8);
        }

        // push api
        cfClient.pushApplication(tag + "-todos-api", "todos-api-" + version + ".jar")
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-api").build())).subscribe();

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
                                .variableValue("https://" + tag + "-todos-api." + cfClient.domain())
                                .build()))
                .then(cfClient.ops().applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-edge").build())).subscribe();
    }
}
