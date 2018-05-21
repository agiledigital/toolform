package au.com.agiledigital.toolform.model

/**
  * Resources are not buildable components. They are expected to be provided externally by the environment that the
  * project is deployed into. For example, a database is a resource.
  *
  * Developers can provided mappings between a resource and the external resource in their 'dev-resources' file. This
  * will convert the Resource into a MappedResource, which is a Composable element.
  *
  * @param id            the id of the resource.
  * @param resourceType  the type of the resource (e.g. postgres, elastic search)
  * @param settings      additional settings for the resource.
  * @param image         the docker URL for the image associated with this resource.
  * @param environment   a map of environment variables to inject into the container.
  * @param exposedPorts a list of ports that are exposed to other services in the internal network.
  * @param externalPorts  a list of ports that are exposed to an outside network.
  *                      (This is usually done through the edge/nginx reverse proxy but sometimes you need to expose a container)
  */
final case class Resource(id: String,
                          resourceType: String,
                          settings: Option[ResourceSettings],
                          image: Option[String],
                          environment: Map[String, String] = Map(),
                          exposedPorts: List[PortMapping] = List(),
                          externalPorts: List[PortMapping] = List(),
                          storage: Option[String])
    extends ToolFormService {
  val tagName: String = id

  def noSettingsSpecified(): Exception =
    new IllegalArgumentException(s"""[${this.id}] resource requires a settings object""")

  def unsupportedAccessMode(modes: List[String]): Exception =
    new IllegalArgumentException(s"""Unsupported access mode(s) [${modes.mkString(",")}] for resource [${this.id}]""")
}

/**
  * A MappedResource is a Resource that has been mapped by the developer and can be Composed into the .yml config.
  *
  * @param path        the full path to the mapped resource config.
  * @param tagName     the name of the docker image used to provide the resource.
  * @param environment the environment variables to pass to the docker image.
  * @param ports       the ports that will be exposed by the resource docker.
  * @param links       the links that will be made to the resource docker.
  * @param resource    the resource that was mapped.
  */
final case class MappedResource(path: String, tagName: String, environment: Map[String, String], ports: Seq[String], links: Seq[String], resource: Resource) extends ProjectElement {

  override def id: String = path

}

final case class ResourceSettings(accessModes: List[String] = List(), paths: List[String] = List())
