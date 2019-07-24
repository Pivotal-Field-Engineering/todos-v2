# README

## Todo(s) UI

This repo contains the [todomvc.com](http://todomvc.com), Vue.js implementation, which is used to interact with [Todo(s) API](https://github.com/corbtastik/todos-api.git) directly or the [Todo(s) Gateway](https://github.com/corbtastik/todos-gateway.git).  The major difference between this version and the one on [todomvc.com](http://todomvc.com) is it calls a backing API to create, retrieve, update and delete Todo(s). This amounts to using ``VueResource`` to call the backing API from Vue.js, that and a few log statements are the only differences.

### Primary dependencies

* [Vue.js](http://vuejs.org) (front end framework)
* [VueResource](https://github.com/pagekit/vue-resource) (HTTP client for Vue.js)

### Prerequisite(s)

* Node.js and NPM to pull dependencies, Node.js is **not** used to run this app
* An HTTP Server, this doc shows [http-server](https://github.com/indexzero/http-server) which is a command line HTTP Server

### Build

This Application is JavaScript, HTML and CSS so there really isn't a build per-se.  However Node.js/NPM is used to pull JavaScript and CSS dependencies such as Vue.js, VueResource and TodoMVC libs.  
``package.json`` defines dependencies for this Application.

```json
{
  "private": true,
  "dependencies": {
    "director": "^1.2.0",
    "todomvc-app-css": "^2.0.0",
    "todomvc-common": "^1.0.1",
    "vue": "^2.1.8",
    "vue-resource": "^1.5.0"
  }
}
```

```bash
# Optional - install http-server if you're gonna use it to run this App
> npm install -g http-server
# Get the code
> git clone https://github.com/corbtastik/todos-ui.git
> cd todos-ui
# Install dependencies into node_modules
# Note node_modules is referenced in index.html
> npm install
```

### Run

After Building, start ``http-server`` from the ``todos-ui`` directory to boot the UI.  By default use port ``4040``.

```bash
> http-server -p 4040 .
Starting up http-server, serving .
Available on:
  http://127.0.0.1:4040
  http://10.0.1.8:4040
Hit CTRL-C to stop the server
```

### Verify

Once Todo(s) UI is running open ``http://localhost:4040`` in a browser (Chrome :wink: :wink:).

#### API Offline  

If running Todo(s) UI in offline mode (i.e. ``/api/todos`` ``404'd``) then data entered will be saved to a local array and lost when the browser closes.  Todo(s) UI will connect with [Todo(s) Gateway](https://github.com/corbtastik/todos-gateway) if running locally and if [Todo(s) API](https://github.com/corbtastik/todos-api) is also running then we can save Todo(s) entered from the browser.

<p align="center">
    <img src="https://github.com/corbtastik/todos-images/raw/master/todos-ui/todos-ui-offline.png" width="640">
</p>

#### API Online

Note - make sure to load the UI from the proxy (i.e. ``localhost:9999``)

<p align="center">
    <img src="https://github.com/corbtastik/todos-images/raw/master/todos-ui/todos-ui-online.png" width="640">
</p>

## Run on PAS

[Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) is a modern runtime for Java, .NET, Node.js apps and many more, that provides a connected 5-star development to delivery experience.  PAS provides a cloud agnostic surface for delivering apps, apps such as Spring Boot Microservices...and...wait for it...Single Page Apps :bulb: like those on [todomvc.com](http://todomvc.com/).  Rarely in computing do we see this level of support across multiple application development frameworks in harmony with a platform.  Its supersonic dev to delivery with only Cloud Native principles as the interface.

The gist is PAS is great for Frontend Apps built in the latest bits :smile:

### manifest.yml & vars.yml

The only PAS specific artifacts in this code repo are ``manifest.yml`` and ``vars.yml``.  Modify ``vars.yml`` to add properties **specific to your PAS environment**. See [Variable Substitution](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html#multi-manifests) for more information.  The gist is we only need to set values for our PAS deployment in ``vars.yml`` and pass that file to ``cf push``.

The Todo(s) UI sets 1 optional variable

1. ``APP_VERSION`` - Todo(s) UI version

### manifest.yml

```yml
---
applications:
- name: ((app.name))
  memory: ((app.memory))
  routes:
  - route: ((app.route))
  buildpack: staticfile_buildpack
  env:
    ((env-key-1)): ((env-val-1))
```  

### vars.yml

```yml
app:
  name: todos-ui
  memory: 512MB
  route: todos-ui.cfapps.io
env-key-1: APP_VERSION
env-val-1: 1.0.0.SNAP
```

## cf push...awe yeah  

Yes you can go from zero to hero with one command :sparkling_heart:

Make sure you're in the Todo(s) UI project root (folder with ``manifest.yml``) and ``cf push``...awe yeah! :sunglasses:

```bash
> cf push --vars-file ./vars.yml
```

```bash
> cf app todos-ui
Showing health and status for app todos-ui in org bubbles / space dev as ...

name:              todos-ui
requested state:   started
instances:         1/1
usage:             512M x 1 instances
routes:            todos-ui.cfapps.io
last uploaded:     Sat 23 Jun 22:50:59 CDT 2018
stack:             cflinuxfs2
buildpack:         staticfile_buildpack

     state     since                  cpu    memory          disk        details
#0   running   2018-06-24T03:51:31Z   0.1%   12.1M of 512M   56M of 1G
```  

### Verify on Cloud  

Once Todo(s) UI is running, check app versioning.

```bash
> cf env todos-ui | grep APP_VERSION
APP_VERSION: 1.0.0.SNAP
```  

#### Go in peace and make cloudy Todo(s)

Open the Todo(s) UI in your browser ``http://todos-ui.cfapps.io`` (:heavy_exclamation_mark: your route will be different)

<p align="center">
    <img src="https://github.com/corbtastik/todos-images/raw/master/todos-ui/todos-ui-offline-cloudy.png" width="640">
</p>

Try opening it from the gateway ``http://todos-gateway.cfapps.io`` which will put it in-front of the ``/api``.

<p align="center">
    <img src="https://github.com/corbtastik/todos-images/raw/master/todos-ui/todos-ui-online-cloudy.png" width="640">
</p>

### Stay Frosty  

#### Celebrate - [Yub Nub!](https://www.youtube.com/watch?v=np6vAuS0KNs)  

## Vue.js TodoMVC Example (original readme content)

> Vue.js is a library for building interactive web interfaces.
It provides data-driven, nestable view components with a simple and flexible API.

> _[Vue.js - vuejs.org](http://vuejs.org)_

## Learning Vue.js

The [Vue.js website](http://vuejs.org/) is a great resource to get started.

Here are some links you may find helpful:

* [Official Guide](http://vuejs.org/guide/)
* [API Reference](http://vuejs.org/api/)
* [Examples](http://vuejs.org/examples/)
* [Building Larger Apps with Vue.js](http://v1.vuejs.org/guide/application.html)

Get help from other Vue.js users:

* [Vue.js on Twitter](https://twitter.com/vuejs)
* [Vue.js on Gitter](https://gitter.im/vuejs/vue)
* [Vue.js Forum](http://forum.vuejs.org)

_If you have other helpful links to share, or find any of the links above no longer work, please [let us know](https://github.com/tastejs/todomvc/issues)._

## Credit

This TodoMVC application was created by [Evan You](http://evanyou.me).
