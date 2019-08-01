### Scaling Applications

#### Manual Scaling

1.  Let’s increase the number of running application instances to for the backend API to 3:

        $ cf scale -i 3 <YOUR>-todos-api
        Scaling app abz-todos-api in org pivot-azwickey / space playground as azwickey@pivotal.io...
        OK

    In reporting `OK`, the CLI is letting you know that the additional
    requested instances have been started, but they are not yet
    necessarily running.

2.  We can determine how many instances are actually running like this:

        $ cf app abz-todos-api       
          Showing health and status for app abz-todos-api in org pivot-azwickey / space playground as azwickey@pivotal.io...
          
          name:                abz-todos-api
          requested state:     started
          isolation segment:   iso-01
          routes:              abz-todos-api.apps.pcfone.io
          last uploaded:       Wed 31 Jul 16:01:00 EDT 2019
          stack:               cflinuxfs3
          buildpacks:          java_buildpack_offline
          
          type:           web
          instances:      1/3
          memory usage:   1024M
               state      since                  cpu    memory         disk           details
          #0   running    2019-07-31T20:09:27Z   1.8%   301.1M of 1G   174.4M of 1G   
          #1   starting   2019-08-01T13:29:06Z   0.5%   267.9M of 1G   174.4M of 1G   
          #2   starting   2019-08-01T13:29:06Z   1.0%   245.1M of 1G   174.4M of 1G   

    -   This application instance has completed the startup process and
        is actually able to accept requests.

    -   This application instance is still starting and will not have
        any requests routed to it.

3.  Eventually all instances will converge to a running state

#### Auto Scaling

1.  You may also setup scaling to occur automatically based on metrics or a schedule.  Within the apps manager UI click on `ENABLE AUTOSCALING` for one of your applications

    ![](auto1.png)
    
2.  The application will automatically begin scaling immediately because the autoscaler has a default setting  of a minimum of 2 application instances.  Click on `manage autoscaling` to explore the options to create metrics-based rules.

    ![](auto2.png)

### Internal Routes on PCF

We can restrict access to the Backend API and UI by removing public routes for those apps and then mapping them to an internal domain (``apps.internal``).  Once the apps have an internal route we can add a network policy that allows the Edge to call them.

1. Repeat pushing `YOUR-todos-api` and `YOUR-todos-webui` but this time set the [domain](https://docs.pivotal.io/pivotalcf/devguide/deploy-apps/routes-domains.html) to an internal one.

    ```bash
    # YOUR-todos-api
    cd ~/Desktop/todos-apps/todos-api
    cf domains
    > name                 status   details
    > apps.retro.io        shared          
    > mesh.apps.retro.io   shared          
    > apps.internal        shared   internal
    cf push your-todos-api -d apps.internal
    ```

    ```bash
    # YOUR-todos-webui
    cd ~/Desktop/todos-apps/todos-webui
    cf push your-todos-webui -d apps.internal
    ```

1. Use cf set-env to update endpoints on `YOUR-todos-edge` to the internal ones

    ```bash
    # YOUR-todos-edge
    cd ~/Desktop/todos-apps/todos-edge
    cf set-env your-todos-edge \
        TODOS_API_ENDPOINT http://your-todos-api.apps.internal:8080
    cf set-env your-todos-edge \
        TODOS_UI_ENDPOINT http://your-todos-webui.apps.internal:8080
    ```

1. Un-map public routes for `YOUR-todos-api` and `YOUR-todos-webui`

    ```bash
    # unmap the public routes for API and UI
    cd ~/Desktop/todos-apps/todos-edge
    cf unmap-route your-todos-api apps.retro.io --hostname your-todos-api
    cf unmap-route your-todos-webui apps.retro.io --hostname your-todos-webui
    ```

1. Restage `YOUR-todos-edge`

    ```bash
    cd ~/Desktop/todos-apps/todos-edge
    cf restage your-todos-edge
    ```

1. Add network policy to allow access to `YOUR-todos-api` and `YOUR-todos-webui` from only `YOUR-todos-edge`

    ```bash
    cd ~/Desktop/todos-apps/todos-edge
    cf add-network-policy your-todos-edge --destination-app your-todos-api
    cf add-network-policy your-todos-edge --destination-app your-todos-webui
    cf network-policies
    Listing network policies in org retro / space arcade as corbs...
    source       destination   protocol   ports
    todos-edge   todos-api     tcp        8080
    todos-edge   todos-webui   tcp        8080
    ```

1. Open `YOUR-todos-edge` in Chrome
    * You'll have a route to your `YOUR-todos-edge` app...for example `https://your-todos-edge.apps.retro.io`

### Logging and Monitoring

One of the most important enablers of visibility into application
behavior is logging. Effective management of logs has historically been
very difficult. Cloud Foundry’s [log
aggregation](https://github.com/cloudfoundry/loggregator) components
simplify log management by assuming responsibility for it. Application
developers need only log all messages to either `STDOUT` or `STDERR`,
and the platform will capture these messages.

Application developers can view application logs using the CF CLI.

1.  Let’s view recent log messages for the application. For this you
    can use any of the microservices deployed:

        $ cf logs abz-todos-api --recent

    Here are two interesting subsets of one output from that command:

        2019-08-01T09:59:38.69-0400 [RTR/1] OUT abz-todos-api.apps.pcfone.io - [2019-08-01T13:59:38.559+0000] "GET /favicon.ico HTTP/1.1" 200 0 946 "https://abz-todos-api.apps.pcfone.io/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36" "192.168.4.4:53777" "192.168.16.39:61018" x_forwarded_for:"10.255.204.78, 192.168.4.4" x_forwarded_proto:"https" vcap_request_id:"c0b2df34-9d20-4806-4bc1-9250ff88ed0d" response_time:0.140045137 app_id:"37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9" app_index:"1" x_b3_traceid:"0caff563e5631f3e" x_b3_spanid:"0caff563e5631f3e" x_b3_parentspanid:"-" b3:"0caff563e5631f3e-0caff563e5631f3e"
        2019-08-01T09:59:38.69-0400 [RTR/1] OUT
        2019-08-01T09:59:59.81-0400 [RTR/2] OUT abz-todos-api.apps.pcfone.io - [2019-08-01T13:59:57.649+0000] "GET /kill HTTP/1.1" 502 0 67 "-" "curl/7.54.0" "192.168.4.4:39555" "192.168.16.39:61018" x_forwarded_for:"10.255.204.78, 192.168.4.4" x_forwarded_proto:"https" vcap_request_id:"975852f7-bfb9-4c37-5e22-902133069864" response_time:2.16848654 app_id:"37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9" app_index:"1" x_b3_traceid:"207b863b74b48833" x_b3_spanid:"207b863b74b48833" x_b3_parentspanid:"-" b3:"207b863b74b48833-207b863b74b48833"
        ...   
        2019-08-01T10:13:55.10-0400 [API/0] OUT Updated app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9 ({"state"=>"STARTED"})
        2019-08-01T10:14:06.14-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:14:06.139  INFO [todos-api,,,] 23 --- [           main] io.todos.api.TodosAPI                    : TodosAPI booting with todos.api.limit=1024
        2019-08-01T10:14:06.16-0400 [APP/PROC/WEB/0] OUT 2019-08-01 14:14:06.169  INFO [todos-api,,,] 38 --- [           main] io.todos.api.TodosAPI                    : TodosAPI booting with todos.api.limit=1024
        2019-08-01T10:21:09.90-0400 [API/3] OUT Updated app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9 ({"buildpack"=>"java_buildpack_offline", "disk_quota"=>1024, "environment_json"=>"[PRIVATE DATA HIDDEN]", "health_check_http_endpoint"=>"", "health_check_type"=>"port", "instances"=>2, "memory"=>1024, "name"=>"abz-todos-api", "space_guid"=>"f5a69750-6b54-4573-96f5-133c3a6cdc49"})
        2019-08-01T10:21:12.01-0400 [API/1] OUT Uploading bits for app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9
        2019-08-01T10:21:22.15-0400 [API/1] OUT Updated app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9 ({"state"=>"STOPPED"})
        2019-08-01T10:21:22.42-0400 [API/2] OUT Creating build for app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9
        2019-08-01T10:21:22.54-0400 [API/2] OUT Updated app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9 ({"state"=>"STARTED"})
        2019-08-01T10:21:42.81-0400 [API/4] OUT Creating droplet for app with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9
        ...
        2019-08-01T10:21:50.88-0400 [CELL/1] OUT Starting health monitoring of container
        2019-08-01T10:21:51.12-0400 [APP/PROC/WEB/0] OUT JVM Memory Configuration: -Xmx399529K -Xss1M -XX:ReservedCodeCacheSize=240M -XX:MaxDirectMemorySize=10M -XX:MaxMetaspaceSize=137046K

    -   An “Apache-style” access log event from the (Go)Router

    -   An API log event that corresponds to an event as shown in
        `cf events`

    -   A CELL log event indicating the start of an application instance
        on that DEA.

    <!-- -->

         2019-08-01T10:22:00.11-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:22:00.113  INFO [todos-api,,,] 34 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
         2019-08-01T10:22:00.16-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:22:00.160  INFO [todos-api,,,] 34 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
         2019-08-01T10:22:00.16-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:22:00.161  INFO [todos-api,,,] 34 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.19]
         2019-08-01T10:22:00.29-0400 [APP/PROC/WEB/0] OUT 2019-08-01 14:22:00.297  INFO [todos-api,,,] 15 --- [           main] .s.c.s.e.EurekaInstanceAutoConfiguration : Eureka registration method: route
         2019-08-01T10:22:00.31-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:22:00.315  INFO [todos-api,,,] 34 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
         2019-08-01T10:22:00.31-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:22:00.316  INFO [todos-api,,,] 34 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 3501 ms
         ...
         2019-08-01T10:22:05.20-0400 [APP/PROC/WEB/0] OUT 2019-08-01 14:22:05.200  INFO [todos-api,,,] 15 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
         2019-08-01T10:22:05.20-0400 [APP/PROC/WEB/0] OUT 2019-08-01 14:22:05.201  INFO [todos-api,,,] 15 --- [           main] .s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8080
         2019-08-01T10:22:05.20-0400 [APP/PROC/WEB/0] OUT 2019-08-01 14:22:05.204  INFO [todos-api,,,] 15 --- [           main] io.todos.api.TodosApp                    : Started TodosApp in 12.85 seconds (JVM running for 14.071)

    As you can see, Cloud Foundry’s log aggregation components capture
    both application logs and CF component logs relevant to your
    application. These events are properly interleaved based on time,
    giving you an accurate picture of events as they transpired across
    the system.

2.  To get a running “tail” of the application logs rather than a dump,
    simply type(replace with the name of your deployed application):

        $ cf logs abz-todos-api 

    You can try various things like refreshing the browser and
    triggering stop/start events to see logs being generated.

### High Availability

Cloud Foundry’s [Heatlh
Manager](http://docs.cloudfoundry.org/concepts/architecture/#hm9k)
actively monitors the health of our application processes and will
restart them should they crash.

1.  If you don’t have one already running, start a log tail for you API backend:

        $ cf logs <YOUR>-todos-api 

2.  After the previous scaling exercises you should have multiple instances of the application runningIf you do not have more than one application instance running, execute the scale command to scale to 2 or more application instances. Invoke a http request to the `Kill` rest endpoint.  This will trigger a JVM exit with an error code (`System.exit(1)`), causing the Health Manager to observe
    an application instance crash:

    ```bash
    $ curl https://abz-todos-api.apps.pcfone.io/kill    
    ```

3.  After invoking kill switch a couple of interesting things should
    happen. First, you’ll see an error code returned, as the request you submitted never returns a response:

    ```bash
    502 Bad Gateway: Registered endpoint failed to handle the request.    
    ```

    Also, if you’re paying attention to the log tail, you’ll see some interesting log messages fly by:

           2019-08-01T10:01:20.14-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:01:20.148 DEBUG [todos-api,c10525dbb0e67e3c,c10525dbb0e67e3c,false] 15 --- [nio-8080-exec-4] io.todos.api.TodosAPI                    : Killing the process.  BYE!!
           2019-08-01T10:01:20.16-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:01:20.165  INFO [todos-api,,,] 15 --- [       Thread-6] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'applicationTaskExecutor'
           2019-08-01T10:01:22.31-0400 [RTR/1] OUT abz-todos-api.apps.pcfone.io - [2019-08-01T14:01:20.011+0000] "GET /kill HTTP/1.1" 502 0 67 "-" "curl/7.54.0" "192.168.4.4:53903" "192.168.16.36:61106" x_forwarded_for:"10.255.204.78, 192.168.4.4" x_forwarded_proto:"https" vcap_request_id:"529aac0f-8fcb-4586-74c9-50b1751ea87b" response_time:2.30435294 app_id:"37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9" app_index:"1" x_b3_traceid:"c10525dbb0e67e3c" x_b3_spanid:"c10525dbb0e67e3c" x_b3_parentspanid:"-" b3:"c10525dbb0e67e3c-c10525dbb0e67e3c"
           2019-08-01T10:01:22.31-0400 [RTR/1] OUT
           2019-08-01T10:01:25.85-0400 [HEALTH/1] ERR Failed to make TCP connection to port 8080: connection refused
           2019-08-01T10:01:25.85-0400 [CELL/1] OUT Container became unhealthy
           2019-08-01T10:01:25.86-0400 [CELL/SSHD/1] OUT Exit status 0
           2019-08-01T10:01:27.36-0400 [APP/PROC/WEB/1] OUT Exit status 1
           2019-08-01T10:01:32.60-0400 [CELL/1] OUT Cell b1835ad3-904e-4c23-8b5b-552bd9b6d8ca stopping instance 56f869e3-f895-4d3c-7c9e-c74f
           2019-08-01T10:01:32.60-0400 [CELL/1] OUT Cell b1835ad3-904e-4c23-8b5b-552bd9b6d8ca destroying container for instance 56f869e3-f895-4d3c-7c9e-c74f
           2019-08-01T10:01:32.60-0400 [API/0] OUT Process has crashed with type: "web"
           2019-08-01T10:01:32.61-0400 [API/0] OUT App instance exited with guid 37d5a9ff-ba09-4c4b-9e93-3621ff31e2c9 payload: {"instance"=>"56f869e3-f895-4d3c-7c9e-c74f", "index"=>1, "cell_id"=>"b1835ad3-904e-4c23-8b5b-552bd9b6d8ca", "reason"=>"CRASHED", "exit_description"=>"Instance became unhealthy: Failed to make TCP connection to port 8080: connection refused", "crash_count"=>2, "crash_timestamp"=>1564668092577786353, "version"=>"3e8e147b-d56b-4c88-aebe-f6a4639e75d5"}
           2019-08-01T10:01:32.66-0400 [CELL/1] OUT Cell 8851f31c-1d22-48d4-b987-839b9a8db015 creating container for instance 3e7255f4-03c6-4569-54e7-d71f
           2019-08-01T10:01:32.77-0400 [PROXY/1] OUT Exit status 137
           2019-08-01T10:01:33.01-0400 [CELL/1] OUT Cell b1835ad3-904e-4c23-8b5b-552bd9b6d8ca successfully destroyed container for instance 56f869e3-f895-4d3c-7c9e-c74f  

    -   Just before issuing the `System.exit(1)` call, the application
        logs that the kill switch was clicked.

    -   The (Go)Router logs the 502 error.

    -   The API logs that an application instance exited due to a crash.

4.  Wait a few seconds… By this time you should have noticed some additional interesting events in the logs:

        2019-08-01T10:01:33.10-0400 [CELL/1] OUT Cell 8851f31c-1d22-48d4-b987-839b9a8db015 successfully created container for instance 3e7255f4-03c6-4569-54e7-d71f
           2019-08-01T10:01:33.29-0400 [CELL/1] OUT Downloading droplet...
           2019-08-01T10:01:36.34-0400 [CELL/1] OUT Downloaded droplet (72.7M)
           2019-08-01T10:01:36.44-0400 [CELL/1] OUT Starting health monitoring of container
           ...
           2019-08-01T10:01:44.57-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:01:44.576  INFO [todos-api,,,] 14 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
           2019-08-01T10:01:44.57-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:01:44.578  INFO [todos-api,,,] 14 --- [           main] io.todos.api.TodosApp                    : Started TodosApp in 6.893 seconds (JVM running for 7.952)
           2019-08-01T10:01:44.57-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:01:44.579  INFO [todos-api,,,] 14 --- [           main] io.todos.api.TodosApp                    : ApplicationStartedEvent @ 1564668104579
           2019-08-01T10:01:44.59-0400 [APP/PROC/WEB/1] OUT 2019-08-01 14:01:44.594  INFO [todos-api,,,] 14 --- [           main] io.todos.api.TodosApp                    : ApplicationReadyEvent @ 1564668104594
           2019-08-01T10:01:45.05-0400 [CELL/1] OUT Container became healthy  

    -   The PCF indicates that it is starting another instance of the application as a result of the Health Manager observing a difference between the desired and actual state.

    -   The new application instance starts logging events as it starts
        up.

### Viewing Application *Events*

PCF only allows application configuration to be modified via
its API. This gives application operators confidence that all changes to
application configuration are known and auditable. It also reduces the
number of causes that must be considered when problems arise.

All application configuration changes are recorded as *events*. These
events can be viewed via the Cloud Foundry API, and viewing is
facilitated via the CLI.

Take a look at the events that have transpired so far for our deployment
of the todos-api:

    $ cf events abz-todos-api
      Getting events for app abz-todos-api in org pivot-azwickey / space playground as azwickey@pivotal.io...
      
      time                          event                      actor                 description
      2019-08-01T10:01:32.00-0400   app.crash                  abz-todos-api         index: 1, reason: CRASHED, cell_id: b1835ad3-904e-4c23-8b5b-552bd9b6d8ca, instance: 56f869e3-f895-4d3c-7c9e-c74f, exit_description: Instance became unhealthy: Failed to make TCP connection to port 8080: connection refused
      2019-08-01T10:01:32.00-0400   audit.app.process.crash    web                   index: 1, reason: CRASHED, cell_id: b1835ad3-904e-4c23-8b5b-552bd9b6d8ca, instance: 56f869e3-f895-4d3c-7c9e-c74f, exit_description: Instance became unhealthy: Failed to make TCP connection to port 8080: connection refused
      2019-08-01T10:00:23.00-0400   app.crash                  abz-todos-api         index: 0, reason: CRASHED, cell_id: f89ba8b1-4867-452a-ab2d-3cf0a3646c05, instance: 79e47f2e-6e4b-4b12-6396-1c4b, exit_description: Instance became unhealthy: Failed to make TCP connection to port 8080: connection refused
      2019-08-01T10:00:23.00-0400   audit.app.process.crash    web                   index: 0, reason: CRASHED, cell_id: f89ba8b1-4867-452a-ab2d-3cf0a3646c05, instance: 79e47f2e-6e4b-4b12-6396-1c4b, exit_description: Instance became unhealthy: Failed to make TCP connection to port 8080: connection refused
      2019-08-01T10:00:10.00-0400   app.crash                  abz-todos-api         index: 1, reason: CRASHED, cell_id: 88729480-3de6-46b1-a0ff-8f17f80a61a1, instance: c5b8eb7c-8a0a-4453-7221-cc2d, exit_description: APP/PROC/WEB: Exited with status 1
      2019-08-01T10:00:10.00-0400   audit.app.process.crash    web                   index: 1, reason: CRASHED, cell_id: 88729480-3de6-46b1-a0ff-8f17f80a61a1, instance: c5b8eb7c-8a0a-4453-7221-cc2d, exit_description: APP/PROC/WEB: Exited with status 1
      2019-08-01T09:56:58.00-0400   audit.app.droplet.create   azwickey@pivotal.io
      2019-08-01T09:56:39.00-0400   audit.app.update           azwickey@pivotal.io   state: STARTED
      2019-08-01T09:56:39.00-0400   audit.app.build.create     azwickey@pivotal.io
      2019-08-01T09:56:39.00-0400   audit.app.update           azwickey@pivotal.io   state: STOPPED
      2019-08-01T09:56:32.00-0400   audit.app.upload-bits      azwickey@pivotal.io
      2019-08-01T09:56:28.00-0400   audit.app.update           azwickey@pivotal.io   disk_quota: 1024, instances: 2, memory: 1024, environment_json: [PRIVATE DATA HIDDEN]
      2019-08-01T09:40:38.00-0400   audit.app.update           autoscaling_service   instances: 2
      2019-08-01T09:31:06.00-0400   audit.app.update           azwickey@pivotal.io   instances: 1
      2019-08-01T09:29:03.00-0400   audit.app.update           azwickey@pivotal.io   instances: 3
      2019-08-01T09:28:55.00-0400   audit.app.update           azwickey@pivotal.io   instances: 1
      2019-08-01T09:27:33.00-0400   audit.app.update           azwickey@pivotal.io   instances: 3
      2019-08-01T09:08:04.00-0400   audit.app.update           autoscaling_service   instances: 2
      2019-08-01T10:58:01.00-0400   audit.app.map-route        azwickey@pivotal.io
      2019-08-01T16:09:07.00-0400   audit.app.start            azwickey@pivotal.io

-   Events are sorted newest to oldest, so we’ll start from the bottom.
    Here we see the `app.create` event, which created our application’s
    record and stored all of its metadata (e.g. `memory: 1G`).
    
-   The `app.map-route` event records the incoming request to assign a
    route to our application.    

-   This `app.update` event records the resulting change to our
    applications metadata.

-   Remember scaling the application up? This `app.update` event records
    the metadata change `instances: 3`.

-   And here’s the `app.crash` event recording that we encountered a
    crash of an application instance.

1.  Let’s explicitly ask for the application to be stopped:

        $ cf stop abz-todos-api
          Stopping app abz-todos-api in org pivot-azwickey / space playground as azwickey@pivotal.io...
          OK

2.  Now, examine the additional `app.update` event:

        $ cf events abz-todos-api
          Getting events for app abz-todos-api in org pivot-azwickey / space playground as azwickey@pivotal.io...
          
          time                          event                      actor                 description
          2019-08-01T10:10:27.00-0400   audit.app.update           azwickey@pivotal.io   state: STOPPED
          2019-08-01T10:01:32.00-0400   app.crash                  abz-todos-api         index: 1, reason: CRASHED, cell_id: b1835ad3-904e-4c23-8b5b-552bd9b6d8ca, instance: 56f869e3-f895-4d3c-7c9e-c74f, exit_description: Instance became unhealthy: Failed to make TCP connection to port 8080: connection refused
          ...

3.  Start the application again:

        $ cf start abz-todos-api
        
4.  And again, view the additional `app.update` event.

### Talking Points

* Note that only your edge application can communicate with the API and UI and now those deployments aren't over exposed on the network.  This is accomplished by [Container to Container networking](https://docs.pivotal.io/pivotalcf/devguide/deploy-apps/cf-networking.html) in PCF.
