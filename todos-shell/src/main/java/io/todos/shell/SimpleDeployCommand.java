package io.todos.shell;

import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpleDeployCommand {
    private final CfClient cfClient;

    @Autowired
    public SimpleDeployCommand(CfClient cfClient) {
        this.cfClient = cfClient;
    }

    public void push(String application, String tag, String version) {
        cfClient.pushApplication(tag + "-" + application, application + "-" + version + ".jar")
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-" + application).build())).subscribe();
    }

    public void push(String application, String tag, String version, String serviceInstance) {
        cfClient.pushApplication(tag + "-" + application, application + "-" + version + ".jar")
            .then(cfClient.ops().services().bind(BindServiceInstanceRequest.builder()
                .applicationName(tag + "-" + application)
                .serviceInstanceName(serviceInstance)
                .build()))
            .then(cfClient.ops().applications()
                .start(StartApplicationRequest.builder()
                    .name(tag + "-" + application).build())).subscribe();
    }
}
