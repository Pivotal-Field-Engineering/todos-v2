package io.todos.shell;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

@Component
public class CfClient {
    // local folder with 3 sample jars
    @Value("${jars.folder}")
    String jarsFolder;
    // cf api
    @Value("${cf.api}")
    String cfApi;
    // cf domain, default for public networking
    @Value("${cf.domain}")
    String cfDomain;
    // cf default memory for apps
    @Value("${cf.memory:1024}")
    Integer cfMemory;
    // cf operations API
    private CloudFoundryOperations cf;
    private ReactorDopplerClient dopplerClient;

    // autowire operations instance
    @Autowired
    public CfClient(CloudFoundryOperations operations, ReactorDopplerClient dopplerClient) {
        this.cf = operations;
        this.dopplerClient = dopplerClient;
    }

    // explicit wrapped and exposed cf operations, used by Commands in this package
    Mono<Void> pushApplication(String name, String application) {

        return cf.applications()
                .push(PushApplicationRequest.builder()
                        .noStart(true)
                        .memory(this.cfMemory)
                        .name(name)
                        .path(Paths.get(jarsFolder + "/" + application))
                        .build());
    }

    CloudFoundryOperations ops() {
        return this.cf;
    }

    String domain() {
        return this.cfDomain;
    }
}
