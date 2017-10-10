package au.com.agiledigital.toolform.model

import com.typesafe.config.Config

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
case class Component(optionalId: Option[String], path: String, name: String, builder: String, settings: Option[Config], componentGroup: Option[ComponentGroup]) extends ProjectElement {
  override def id: String = optionalId.getOrElse(path)
  val tagName: String     = path + "_" + id
}
