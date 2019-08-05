package io.todos.shell;

import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.StreamRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
@ShellComponent
public class ShellCommands {
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
    // spring application context
    private ApplicationContext context;

    // autowire operations instance
    @Autowired
    public ShellCommands(ApplicationContext context,
        CloudFoundryOperations operations,
        ReactorDopplerClient dopplerClient) {
        this.cf = operations;
        this.dopplerClient = dopplerClient;
        this.context = context;
    }

    @ShellMethod("get a log stream")
    public void logStreams() {
        Flux<Envelope> stream = dopplerClient.stream(StreamRequest.builder()
            .applicationId("e31ef0be-45d6-4f85-8da1-762cbe35f8b5").build());
        stream.subscribe(System.out::println);
    }

    @ShellMethod("Simple Todo App Deploy")
    public void push(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version) {
        context.getBean(SimpleDeployCommand.class).push(tag, version);
    }

    @ShellMethod("Simple Todo App Deploy with private networking")
    public void pushPrivate(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "internal domain (ex: apps.internal", defaultValue = "apps.internal") String internalDomain) {
        context.getBean(PrivateNetworkingDeployCommand.class).push(tag, version,internalDomain);
    }

    @ShellMethod("Todo App with MySQL Deploy")
    public void pushMySQL(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "mysql service instance name (ex: todos-database)", defaultValue = "todos-database") String serviceInstance) {
        context.getBean(SimpleMySqlDeployCommand.class).push(tag, version, serviceInstance);
    }

    @ShellMethod("Todo App with Redis Deploy")
    public void pushRedis(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "redis service instance name (ex: todos-redis)", defaultValue = "todos-redis") String serviceInstance) {
        context.getBean(SimpleRedisDeployCommand.class).push(tag, version, serviceInstance);
    }

    @ShellMethod("push with spring-cloud")
    public void pushScs(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "config-service", defaultValue = "todos-config") String configServiceInstance,
            @ShellOption(help = "registry-service", defaultValue = "todos-registry") String registryServiceInstance) {

        if (tag.length() < 1) {
            tag = UUID.randomUUID().toString().substring(0, 8);
        }

        pushApplication(tag + "-todos-api",
                Paths.get(jarsFolder, "todos-api-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-api")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-api")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-api")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-api")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-api")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-api").build())).subscribe();

        pushApplication(tag + "-todos-webui",
                Paths.get(jarsFolder, "todos-webui-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-webui")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-webui").build())).subscribe();

        pushApplication(tag + "-todos-edge",
                Paths.get(jarsFolder, "todos-edge-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-edge")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-edge").build())).subscribe();
    }


    @ShellMethod("push with spring-cloud and redis")
    public void pushScsMySQL(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "config-service", defaultValue = "todos-config") String configServiceInstance,
            @ShellOption(help = "registry-service", defaultValue = "todos-registry") String registryServiceInstance,
            @ShellOption(help = "mysql service instance name (ex: todos-database)", defaultValue = "todos-database") String databaseServiceInstance) {

        if (tag.length() < 1) {
            tag = UUID.randomUUID().toString().substring(0, 8);
        }

        // push mysql backend app
        pushApplication(tag + "-todos-mysql",
                Paths.get(jarsFolder, "todos-mysql-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-mysql")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-mysql")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-mysql")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-mysql")
                        .serviceInstanceName(databaseServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-mysql")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-mysql")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-mysql").build())).subscribe();

        pushApplication(tag + "-todos-webui",
                Paths.get(jarsFolder, "todos-webui-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-webui")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-webui").build())).subscribe();

        pushApplication(tag + "-todos-edge",
                Paths.get(jarsFolder, "todos-edge-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-edge")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-edge").build())).subscribe();
    }



    @ShellMethod("push with spring-cloud and redis")
    public void pushScsRedis(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "config-service", defaultValue = "todos-config") String configServiceInstance,
            @ShellOption(help = "registry-service", defaultValue = "todos-registry") String registryServiceInstance,
            @ShellOption(help = "redis service instance name (ex: todos-redis)", defaultValue = "todos-redis") String redisServiceInstance) {

        if (tag.length() < 1) {
            tag = UUID.randomUUID().toString().substring(0, 8);
        }

        // push redis backend app
        pushApplication(tag + "-todos-redis",
                Paths.get(jarsFolder, "todos-redis-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-redis")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-redis")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-redis")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-redis")
                        .serviceInstanceName(redisServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-redis")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-redis")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-redis").build())).subscribe();

        pushApplication(tag + "-todos-webui",
                Paths.get(jarsFolder, "todos-webui-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-webui")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-webui").build())).subscribe();

        pushApplication(tag + "-todos-edge",
                Paths.get(jarsFolder, "todos-edge-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-edge")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-edge").build())).subscribe();
    }

    @ShellMethod("push with spring-cloud and look-aside caching")
    public void pushLookaside(
            @ShellOption(help = "tag for hostname") String tag,
            @ShellOption(help = "version (ex: 1.0.0.RELEASE, 1.0.0.SNAP)", defaultValue = "1.0.0.SNAP") String version,
            @ShellOption(help = "config-service", defaultValue = "todos-config") String configServiceInstance,
            @ShellOption(help = "registry-service", defaultValue = "todos-registry") String registryServiceInstance,
            @ShellOption(help = "mysql service instance name (ex: todos-database)", defaultValue = "todos-database") String databaseServiceInstance,
            @ShellOption(help = "redis service instance name (ex: todos-redis)", defaultValue = "todos-redis") String redisServiceInstance,
            @ShellOption(help = "messaging service instance name (ex: todos-messaging)", defaultValue = "todos-messaging") String messagingServiceInstance) {

        if (tag.length() < 1) {
            tag = UUID.randomUUID().toString().substring(0, 8);
        }

        // push scs app backend app
        pushApplication(tag + "-todos-app",
                Paths.get(jarsFolder, "todos-app-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-app")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-app")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-app")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-app")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-app")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-app")
                        .serviceInstanceName(messagingServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-app").build())).subscribe();

        // push mysql backend for Sor
        pushApplication(tag + "-todos-mysql",
                Paths.get(jarsFolder, "todos-mysql-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-mysql")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-mysql")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-mysql")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-mysql")
                        .serviceInstanceName(databaseServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-mysql")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-mysql")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-mysql").build())).subscribe();

        // push redis backend app for Cache
        pushApplication(tag + "-todos-redis",
                Paths.get(jarsFolder, "todos-redis-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-redis")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-redis")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-redis")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-redis")
                        .serviceInstanceName(redisServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-redis")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-redis")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-redis").build())).subscribe();

        pushApplication(tag + "-todos-webui",
                Paths.get(jarsFolder, "todos-webui-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-webui")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-webui")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-webui")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-webui").build())).subscribe();

        pushApplication(tag + "-todos-edge",
                Paths.get(jarsFolder, "todos-edge-" + version + ".jar").toFile().toPath())
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("TRUST_CERTS")
                                .variableValue(cfApi)
                                .build()))
                .then(this.cf.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(tag + "-todos-edge")
                                .variableName("SPRING_APPLICATION_NAME")
                                .variableValue(tag + "-todos-edge")
                                .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(configServiceInstance)
                        .build()))
                .then(this.cf.services().bind(BindServiceInstanceRequest.builder()
                        .applicationName(tag + "-todos-edge")
                        .serviceInstanceName(registryServiceInstance)
                        .build()))
                .then(cf.applications()
                        .start(StartApplicationRequest.builder()
                                .name(tag + "-todos-edge").build())).subscribe();
    }

    @ShellMethod("list jars")
    public List<String> jars() {
        return Arrays.asList(Paths.get(jarsFolder).toFile().list());
    }

    @ShellMethod("list orgs")
    public List<String> orgs() {
        return cf.organizations().list().map(OrganizationSummary::getName).collectList().block();
    }

    @ShellMethod("list spaces")
    public List<String> spaces() {
        return cf.spaces().list().map(SpaceSummary::getName).collectList().block();
    }

    @ShellMethod("list apps")
    public List<String> apps() {
        return cf.applications().list().map(ApplicationSummary::getName).collectList().block();
    }

    @ShellMethod("list services")
    public List<String> services() {
        return cf.services().listInstances().map(ServiceInstanceSummary::getName).collectList().block();
    }

    Mono<Void> pushApplication(String name, Path application) {
        return cf.applications()
                .push(PushApplicationRequest.builder()
                        .noStart(true)
                        .memory(this.cfMemory)
                        .name(name)
                        .path(application)
                        .build());
    }
}
