# toolform

[![Build Status](https://travis-ci.org/agiledigital/toolform.svg?branch=master)](https://travis-ci.org/agiledigital/toolform)

**Toolform** is a *DevOps* utility for defining deployment environments such that software applications may be developed independently of their eventual deployment environment. This means that developers don't need to be experts in the target tools/platforms given that toolform takes care these concerns via configuration.

Practically speaking it is a command-line tool for software teams to define and configure their continuous integration (build/test) and continuous delivery (deploy) environments consistently and efficiently.

Toolform exists to generate "infrastructure as code" config files for popular target environments (e.g. Jenkins2 build pipelines, spinnaker deployment pipelines, openshift projects, kubernetes environments, minikube, etc.). It works with typical developer workflows to ensure as close as possible representation of "production conditions" in all environments (including developer desktops).

Features
===============================================================================

Key features of Toolform include:

* **Infrastructure as code**: Your source project artefacts, runtimes, topology, and services (both in-house and 3rd party) are described using a high-level configuration syntax. This allows a blueprint of your project and all of its runtime dependencies to be versioned and treated as you would any other code;

* **Target platform agnostic**: Whether targeting a developer's local environment, or setting up the full CI/CD pipeline from bare metal, pluggable backends process the definition to the target tool or platform; and

* **Project agnostic**: Toolform is focussed on the topology, pipeline, and environmental services (such as payment gateways) of deployment environments, meaning it doesn't make restrictions as to the specifics of any particular source project software.

Usage
================================================================================

```
toolform --help

Usage:
    toolform inspect
    toolform generate

Generates deployment configuration from a project definition.

Options and flags:
    --help
        Display this help text.
    --version
        Print the version number and exit.

Subcommands:
    inspect
        Inspect and print the content of the project file
    generate
        Generate config files targeting a particular platform
```

Inspect Command
--------------------------------------------------------------------------------
```
toolform inspect --help

Usage: toolform inspect --input <file>

Inspect and print the content of the project file

Options and flags:
    --help
        Display this help text.
    --input <file>, -i <file>
        Input file
```

Generate Command
--------------------------------------------------------------------------------
```
toolform generate --help

Usage:
    toolform generate minikube
    toolform generate dockercompose

Generate config files targeting a particular platform

Options and flags:
    --help
        Display this help text.

Subcommands:
    minikube
        generates config files for Kubernetes (Minikube) container orchestration
    dockercompose
        generates config files for container orchestration

```

```
toolform generate minikube --help

Usage: toolform generate minikube --in-file <file> --out-file <file>

generates config files for Kubernetes (Minikube) container orchestration

Options and flags:
    --help
        Display this help text.
    --in-file <file>, -i <file>
        the path to the project config file
    --out-file <file>, -o <file>
        the path to output the generated file(s)

```

```
toolform generate dockercompose --help

Usage: toolform generate dockercompose --in-file <file> --out-file <file>

generates config files for container orchestration

Options and flags:
    --help
        Display this help text.
    --in-file <file>, -i <file>
        the path to the project config file
    --out-file <file>, -o <file>
        the path to output the generated file(s)
```

Example Usage
--------------------------------------------------------------------------------

To generate docker compose output for a project, run:
`toolform generate dockercompose -i your-project.conf -o ./target-dir/target-file.yaml`

This will generate a docker compose file at the specified path.

Project Configuration
================================================================================

A toolform project is defined in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md) syntax. The configuration is usually split into three separate files: `environment.conf`, `project.conf` and `topology.conf`. The command line tool only accepts a single file as an input but the HOCON format allows for a file to include any number of other files.

environment.conf
--------------------------------------------------------------------------------

This file specifies configuration values that get [substituted](https://github.com/lightbend/config/blob/master/HOCON.md#substitutions) into the other config files using path expressions.
HOCON also supports environment variable subsititution so you can provide a default value that can be overridden by an environment variable.
For example, in the following code snippet  the default port of the API server is set to 8080 but it can also be overwriten by the `API_SERVER_PORT` environment variable.

```
environment.component.api_server.port = 8080
environment.component.api_server.port = ${?API_SERVER_PORT}
```

At the end of this file you can include the other project configuration files like so

```
include "project.conf"
include "topology.conf"
```

project.conf
--------------------------------------------------------------------------------

This file defines all the components and resources that make up a project.

A component is a service that is part of the project, for example, an API server.
A resource is a service that is usually provided by a third party, for example, a Postgres database server.

topology.conf
--------------------------------------------------------------------------------

This file defines how components and resources are connected together and how services are exposed to the outside world.

A link is currently only informational, and defines a link between two services.

An endpoint defines how services and components are exposed outside the cluster.
Endpoints are manifested differently depending on the platform. For example, the OpenShift platform uses routes.
Endpoints are unsupported by some platforms. For example, in Minikube you usually expose services by making them a NodePort and accessing them directly.

Sample Project
--------------------------------------------------------------------------------

The best place to start understanding the project definition format is to look at the example in
src/test/resources/testprojects/realworldsample, starting with the environment.conf file.

