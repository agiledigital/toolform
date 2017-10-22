# toolform

[![Build Status](https://travis-ci.org/agiledigital/toolform.svg?branch=master)](https://travis-ci.org/agiledigital/toolform)

Toolform is a command-line tool for configuring and combining continuous delivery and continuous integration platforms consistently and efficiently. Toolform can manage existing and popular CI/CD platforms as well as developer workflows to ensure an as close as possible representation of production in all environments.

The key features of Toolform are:

* Infrastructure as Code: The source project artefacts, runtimes, topology, and services (both in-house and 3rd party) are described using a high-level configuration syntax. This allows a blueprint of your project and all of its runtime dependencies to be versioned and treated as you would any other code.

* Target platform agnostic: Weather the targeting a developers local environment, or setting up the full CI/CD pipeline from bare metal, pluggable backends process the definition to the target tool or platform

* Project agnostic: The tool cares more about the topology, pipeline, and environmental services (such as payment gateways) than the specifics of the source projects software.
