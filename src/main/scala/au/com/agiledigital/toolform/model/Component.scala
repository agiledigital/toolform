package au.com.agiledigital.toolform.model

import com.typesafe.config.Config
import pureconfig.{CamelCase, ConfigFieldMapping, KebabCase, ProductHint}

/**
  * A Component is a project element that is built the project. It can be converted into a Docker image and composed
  * into the .yml.
  *
  * @param idOverride     the identifier of the component.
  * @param path           the full path to the component in the configuration.
  * @param name           the name of the component.
  * @param builder        the Docker image that will be used to build the component.
  * @param settings       the settings for building the component.
  * @param componentGroup the group that this component is a part of (may be None).
  * @param environment    a map of environment variables to inject into the container.
  * @param exposedPorts  a list of ports that are exposed to other services in the internal network.
  * @param externalPorts   a list of ports that are exposed to an outside network.
  *                       (This is usually done through the edge/nginx reverse proxy but sometimes you need to expose a container)
  */
final case class Component(idOverride: Option[String],
                           path: String,
                           name: String,
                           builder: String,
                           settings: Option[Config],
                           componentGroup: Option[ComponentGroup],
                           environment: Map[String, String] = Map(),
                           exposedPorts: List[PortMapping] = List(),
                           externalPorts: List[PortMapping] = List())
    extends ToolFormService {
  override def id: String = idOverride.getOrElse(path)
}

object Component {

  implicit val fieldMapping: ProductHint[Component] = ProductHint[Component](ConfigFieldMapping(CamelCase, KebabCase).withOverrides("idOverride" -> "id"))
}
