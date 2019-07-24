# README

## Build and Deploy Spring Cloud   

This Part focuses on building out Spring Cloud to support the Todo(s) EcoSystem we deployed in Part 1.  Once completed the Base Set will integrate with [Spring Cloud Config Server](https://github.com/spring-cloud/spring-cloud-config) and [Service Registry](https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka).

### Spring Cloud Set

1. [Config Server](https://github.com/corbtastik/config-server) - Spring Cloud Config Server
2. [Cloud Index](https://github.com/corbtastik/cloud-index) - Spring Cloud Service Discovery

The objective is to build and deploy the [Spring Cloud Set](#spring-cloud-set) on PAS and confirm the [Base Set](/PART_1.md#base-set) integrates with Config and Service Discovery.  We'll be using the [Todo(s) CICD](https://github.com/corbtastik/todos-cicd) project to run bash scripts to handle each stage.

### PrePrep

:heavy_exclamation_mark: If you've not completed [PrePrep](https://github.com/corbtastik/todos-ecosystem/blob/master/PREPREP.md) now would be a good time to do that :smile:

### Clone Spring Cloud Set  

```bash
> cd $TODOS_HOME
> git clone https://github.com/corbtastik/config-server.git
> git clone https://github.com/corbtastik/cloud-index.git
```

### Deploy  

:heavy_exclamation_mark: Make sure to set your git-repo URI in [Config Server]("") ``vars.yml`` before running deploy.  See [Config Server]("") for more information.

Once [PAS Properties](/PREPREP.md#pas-properties) are set it's time to point-and-click and coolout while we burst into the cloud. :sunglasses:

```bash
> cd $TODOS_HOME/todos-cicd/part_2
> ./deploy.sh
```

### Verify Part 2

#### Verify Deployment  

Do a quick sanity check on the deployment.

```bash
> cf apps
Getting apps in org bubbles / space dev as ...
OK

name            requested state   instances   memory   disk   urls
cloud-index     started           1/1         1G       1G     cloud-index.cfapps.io
config-server   started           1/1         1G       1G     config-srv.cfapps.io
```

#### Verify Endpoints  

Call endpoints on the Cloud Index and Config Server.

##### Call ``/ops/info`` endpoint on Spring Boot Microservices.

```bash
> http config-srv.cfapps.io/ops/info
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
X-Vcap-Request-Id: 5ef5b69c-2fc9-442b-77e1-6043abcb7174

{
    "build": {
        "artifact": "config-server",
        "group": "io.corbs",
        "name": "config-server",
        "time": "2018-06-25T15:49:59.559Z",
        "version": "1.0.0.SNAP"
    }
}
```

##### Call Eureka endpoint to list App(s)

If you've completed [Part 1](/PART_1.md) then you should see apps for [Todo(s) Gateway](https://github.com/corbtastik/todos-gateway), [Todo(s) API](https://github.com/corbtastik/todos-api) and now [Config Server](https://github.com/corbtastik/config-server).

```bash
> http cloud-index.cfapps.io/eureka/apps | grep "<app>"  
      <app>TODOS-GATEWAY</app>
      <app>TODOS-API</app>
      <app>CONFIG-SERVER</app>
```

##### Call Eureka endpoint to get Todo(s) API info  

[Part 1](/PART_1.md) deployed an instance of Todo(s) API to PAS which was Spring Cloud ready.  When we bring Cloud Index online, Todo(s) API will register and download the Service Registry from Eureka. Call the eureka endpoint directly and get information about Todo(s) API.  Notice the ``<leaseInfo>`` element which indicates Todo(s) API will check-in with Eureka every 30s.

```bash
> http cloud-index.cfapps.io/eureka/apps/todos-api
HTTP/1.1 200 OK
Content-Type: application/xml
X-Vcap-Request-Id: 3e4e4a36-7771-46f5-66b0-3d62e7c2cf59

<application>
  <name>TODOS-API</name>
  <instance>
    <instanceId>df3e734c-ba96-4b60-6be3-80e2</instanceId>
    <hostName>df3e734c-ba96-4b60-6be3-80e2</hostName>
    <app>TODOS-API</app>
    <ipAddr>10.252.182.241</ipAddr>
    <status>UP</status>
    <overriddenstatus>UNKNOWN</overriddenstatus>
    <port enabled="true">8080</port>
    <countryId>1</countryId>
    <leaseInfo>
      <renewalIntervalInSecs>30</renewalIntervalInSecs>
      <durationInSecs>90</durationInSecs>
      <registrationTimestamp>1529938712317</registrationTimestamp>
      <lastRenewalTimestamp>1529943873950</lastRenewalTimestamp>
      <evictionTimestamp>0</evictionTimestamp>
      <serviceUpTimestamp>1529938712317</serviceUpTimestamp>
    </leaseInfo>
    <metadata>
      <management.port>8080</management.port>
    </metadata>
    <vipAddress>todos-api</vipAddress>
    <lastUpdatedTimestamp>1529938712317</lastUpdatedTimestamp>
    <lastDirtyTimestamp>1529938712308</lastDirtyTimestamp>
    <actionType>ADDED</actionType>
  </instance>
</application>

```  

### Stay Frosty :snowman:

### References

* [Eureka in 10 mins](https://blog.asarkar.org/technical/netflix-eureka/)