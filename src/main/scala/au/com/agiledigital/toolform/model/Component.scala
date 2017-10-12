package au.com.agiledigital.toolform.model

import com.typesafe.config.Config
import pureconfig.{ConfigFieldMapping, ProductHint}

/**
  * A Component is a project element that is built the project. It can be converted into a Docker image and composed
  * into the .yml.
  *
  * @param optionalId     the identifier of the component.
  * @param path           the full path to the component in the configuration.
  * @param name           the name of the component.
  * @param builder        the Docker image that will be used to build the component.
  * @param settings       the settings for building the component.
  * @param componentGroup the group that this component is a part of (may be None).
  */
final case class Component(optionalId: Option[String], path: String, name: String, builder: String, settings: Option[Config], componentGroup: Option[ComponentGroup]) extends ProjectElement {
  override def id: String = optionalId.getOrElse(path)
}

object Component {
  implicit val fieldMapping: ProductHint[Component] = ProductHint[Component](new ConfigFieldMapping {
    def apply(fieldName: String): String = if (fieldName == "optionalId") "id" else fieldName
  })
}
