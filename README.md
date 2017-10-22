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
toolfrom --help

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
        generates config files for container orchestration.
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

Usage: toolform generate --in-file <file> --out-file <file> --generate-docker-compose

generates config files for container orchestration.

Options and flags:
    --help
        Display this help text.
    --in-file <file>, -i <file>
        the path to the project config file
    --out-file <file>, -o <file>
        the path to output the generated file(s)
    --generate-docker-compose, -d
        generate a Docker Compose v3 file as output (default)

```

Example Usage
--------------------------------------------------------------------------------

To generate docker compose output for a project, run:
`toolform generate -i your-project.conf -o ./target-dir -d`

This will generate docker compose files into your `target-dir`.

Project Conf Format
================================================================================

The best place to start understanding the project definition format is to look at the example in
src/test/resources/testprojects/realworldsample, starting with the environment.conf file.

