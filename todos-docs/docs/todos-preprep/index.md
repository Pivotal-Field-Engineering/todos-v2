# README

## Pre-Prep  

Create a top-level directory to house all Todo apps, for example...

* Think of this directory as our ``TODOS_HOME``.

```bash
$ mkdir todos-apps
$ cd todos-apps
$ export TODOS_HOME=`pwd`
```

Fork-n-clone or just clone each project into ``TODOS_HOME`` so they sit at the same level on the file-system.

## Cloud Foundry

You'll need access to a Cloud Foundry deployment, [Pivotal Web Services](https://run.pivotal.io) is a simple way to start and the method used here.

Download and install the [cf-cli](https://github.com/cloudfoundry/cli/releases), if you use version 6.37 and above you can take advantage of Variable Substitution in manifest files, which is used extensively in the samples.

Once the cf-cli is installed, target and authenticate to CF.  Sample below shows the PWS api endpoint.

```bash
$ cf login -a api.run.pivotal.io \
  -u USERNAME -o ORG -s SPACE
```

### Roles

* [Space Developer](https://docs.cloudfoundry.org/concepts/roles.html) is assumed

### Buildpacks

The following buildpacks are used to containerize the sample Todo apps.

* [Java Buildpack](https://github.com/cloudfoundry/java-buildpack) (4.10+)
* [Staticfile Buildpack](https://github.com/cloudfoundry/staticfile-buildpack) (1.4+)
* [Nodejs Buildpack](https://github.com/cloudfoundry/nodejs-buildpack) (1.6.34)

### Services

* MySQL Service
    * On PWS this is [cleardb](https://docs.run.pivotal.io/marketplace/services/cleardb.html)
    * On PCF this is [MySQL for PCF](https://docs.pivotal.io/p-mysql)
* Redis Service
    * On PWS this is [rediscloud](https://docs.run.pivotal.io/marketplace/services/rediscloud.html)
    * On PCF this is [Redis for PCF](https://docs.pivotal.io/redis)
* RabbitMQ Service
    * On PWS this is [cloudamqp](https://docs.run.pivotal.io/marketplace/services/cloudamqp.html)
    * On PCF this is [RabbitMQ for PCF](https://docs.pivotal.io/rabbitmq-cf)

## Kitchen Sink

* JDK 1.8 - _I use [sdkman](https://sdkman.io/) locally_
* Editor or IDE
* Bash
* Git
* [cf-cli](https://github.com/cloudfoundry/cli/releases)
* [PWS](https://run.pivotal.io/)
* [Httpie](https://httpie.org/)
* Chrome or ...
* [Node.js and NPM](https://nodejs.org) - v10.x.x LTS
* [http-server](https://www.npmjs.com/package/http-server) node module or any local HTTP server