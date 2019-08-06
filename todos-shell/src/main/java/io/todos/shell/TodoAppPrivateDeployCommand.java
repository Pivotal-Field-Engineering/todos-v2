package io.todos.shell;

import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.routes.UnmapRouteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TodoAppPrivateDeployCommand {
    private final CfClient cfClient;

    @Autowired
    public TodoAppPrivateDeployCommand(CfClient cfClient) {
        this.cfClient = cfClient;
    }

    public void push(String tag, String version, String internalDomain) {

        // push api with internal route
        cfClient.pushApplication(tag + "-todos-api", "todos-api-" + version + ".jar")
            .then(cfClient.ops().routes()
                .map(MapRouteRequest.builder()
                    .applicationName(tag + "-todos-api")
                    .domain(internalDomain)
                    .host(tag + "-todos-api")
                    .build()))
            .then(cfClient.ops().routes()
                .unmap(UnmapRouteRequest.builder()
                    .applicationName(tag + "-todos-api")
                    .domain(cfClient.domain())
                    .host(tag + "-todos-api")
                    .build()))
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-api").build())).subscribe();

        // push webui with internal route
        cfClient.pushApplication(tag + "-todos-webui", "todos-webui-" + version + ".jar")
            .then(cfClient.ops().routes()
                .map(MapRouteRequest.builder()
                    .applicationName(tag + "-todos-webui")
                    .domain(internalDomain)
                    .host(tag + "-todos-webui")
                    .build()))
            .then(cfClient.ops().routes()
                .unmap(UnmapRouteRequest.builder()
                    .applicationName(tag + "-todos-webui")
                    .domain(cfClient.domain())
                    .host(tag + "-todos-webui")
                    .build()))
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-webui").build())).subscribe();

        // push edge and config UI and API endpoints in edge's ENV
        cfClient.pushApplication(tag + "-todos-edge", "todos-edge-" + version + ".jar")
            .then(cfClient.ops().applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                    .name(tag + "-todos-edge")
                    .variableName("TODOS_UI_ENDPOINT")
                    .variableValue("http://" + tag + "-todos-webui.apps.internal:8080")
                    .build()))
            .then(cfClient.ops().applications()
                .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                    .name(tag + "-todos-edge")
                    .variableName("TODOS_API_ENDPOINT")
                    .variableValue("http://" + tag + "-todos-api.apps.internal:8080")
                    .build()))
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-todos-edge").build())).subscribe();
    }

}
