# README

## Todo(s) Concourse

Howdy and welcome.  What is this?

This repository contains Concourse pipelines to automate release and delivery of the Todo set of apps and such.  Concourse is pipelining for a more civilized age, it's built with containers in mind and thus every Resource in Concourse is a container, whether it be to run a bunch of bash scripts or interacting with git repositories.  Central concepts are the Pipeline, Resources, Tasks and Jobs, all must-have primitives for ci/cd.

Go my friend to one of these fine resources...get Concourse and fly.

1. (Concourse CI)[https://concourse-ci.org/]
2. (Stark and Wayne Concourse Tutorial)[https://concoursetutorial.com]

## Concourse set-up

You need a Concourse setup, pick your flavor and create a new one :), samples below show locally deployed Concourse via Docker.

1. Concourse via Docker (shown here)
2. BOSH deployed Concourse
3. PCF deployed Concourse

## Using the pipelines

The Pipeline assume the existence of **source-code**, in the context of the Todo set of apps such **source-code** is backed back individual git repositories.  Additionally the **source-code** in the actual repository is assumed to be maven based with build-info enabled.

### Clone this repository

docker-compose up -d
