package au.com.agiledigital.toolform.model

import pureconfig.{ConfigConvert, ConfigFieldMapping, ProductHint}
import pureconfig.ConfigConvert.viaStringOpt

/**
  * Configuration for a project.
  *
  * @param id         the id of the project.
  * @param name       the name of the project.
  * @param components the components that will be built and combined to form the projects.
  * @param resources  the resources that must be supplied to the project to run.
  * @param topology   the network topology of the environment explains how elements will be related/connected.
  * @param volumes    the volumes that will be mounted into components.
  */
case class Project(id: String,
                   name: String,
                   components: Map[String, Component],
                   resources: Map[String, Resource],
                   topology: Topology,
                   volumes: Option[Seq[Volume]],
                   componentGroups: Option[Seq[ComponentGroup]])

/**
  * the network topology of the environment explains how elements will be related/connected.
  * @param links the links between components, other components and resources.
  * @param edges the edges that will make the components and resources available outside the project.
  */
case class Topology(links: Seq[Link], edges: Map[String, Edge])

object Topology {
  implicit val fieldMapping: ProductHint[Topology] = ProductHint[Topology](new ConfigFieldMapping {
    def apply(fieldName: String): String = fieldName
  })
}

/**
  * A reference points to an element of the project that is defined elsewhere.
  * See Link, Volume, Location.
  * @param ref A dot separated path from the project root to the referenced object.  eg. "components.componentA"
  */
case class Reference(ref: String)

object Reference {
  implicit val converter: ConfigConvert[Reference] =
    viaStringOpt(s => Some(Reference(s)), _.toString)
}

/**
  * A link between two Taggable components.
  *
  * E.g.
  * {
  * from: resources.app_database
  * to: components.public_api
  * }
  *
  * @param from the ProjectElement that will be linked into the to ProjectElement
  * @param to   the ProjectElement that will be supplied the from ProjectElement
  */
case class Link(from: Reference, to: Reference)

/**
  * A link between a volume resource and a Project element.
  *
  * @param from the resource that supplies the volume.
  * @param to   the Project element that will be supplied the volume.
  */
case class Volume(from: Resource, to: Reference)
