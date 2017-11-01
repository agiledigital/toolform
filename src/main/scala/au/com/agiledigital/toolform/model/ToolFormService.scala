package au.com.agiledigital.toolform.model

/**
  * Anything that can expose ports to another container or to the outside world.
  * Groups common functionality between Components and Resources.
  *
  * For resources, these properties were previously provided by the `dev-resources.conf` file.
  *
  * Note: This should not be confused with a Kubernetes "service" specification.
  */
trait ToolFormService extends ProjectElement {

  /**
    * A list of ports that are only exposed on the internal network.
    * You would list a port here if you wanted to expose a service to another service.
    * For example, a Mongo database would expose port 27017 so that an API server could access it.
    *
    * Ports listed here will not be exposed to the external network.
    *
    * @return A list of internal ports.
    */
  def exposedPorts: List[PortMapping]

  /**
    * A map of environment variable key value pairs that will be injected into the service at runtime.
    * This will usually be used for overriding behaviours or when a container can only be configured through
    * environment variables rather than by reading out of the project configuration.
    *
    * @return An environment variable map.
    */
  def environment: Map[String, String]

  /**
    * A list of ports that are exposed to the outside world.
    * You would list a port here if you wanted a service to be accessible directly and bypass the load balancer.
    * You do not need to use this list for ports that are only used for inter-service communication.
    *
    * @return A list of external ports.
    */
  def externalPorts: List[PortMapping]
}
