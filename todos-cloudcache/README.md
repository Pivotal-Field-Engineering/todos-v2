# Todos PCC

Pivotal Cloud Cache todos persistence

1. [Pivotal Maven Repo](https://gemfire.docs.pivotal.io/gemfire/getting_started/installation/obtain_gemfire_maven.html)

1. mvnw clean package

1. cf create-service p-cloudcache dev-plan todos-pcc

1. cf create-service-key todos-pcc todos-pcc-client-key

1. cf service-key todos-pcc todos-pcc-client-key (record endpoint and cluster operator creds)

1. connect to PCC (with creds from above)

    ```gfsh>connect --use-http=true --use-ssl --skip-ssl-validation=true --url=https://CHANGEME --user=CHANGEME --password=CHANGEME```
    
1. create "Todos" region ```gfsh>create region --name=Todos --type=REPLICATE```

1. cf push (awe yeah)

1. call endpoints to put/get data `http mytodos-pcc.cfapps.io/ title="Get some Easy Cheese"`