package au.com.agiledigital.toolform.model

import au.com.agiledigital.toolform.model.ReferenceType.ReferenceType
import pureconfig.ConfigConvert

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
final case class Project(id: String,
                         name: String,
                         components: Map[String, Component],
                         resources: Map[String, Resource],
                         topology: Topology,
                         volumes: Option[Seq[Volume]],
                         componentGroups: Option[Seq[ComponentGroup]])

/**
  * The network topology of the environment explains how elements will be related/connected.
  * @param links the links between components, other components and resources.
  * @param edges the edges that will make the components and resources available outside the project.
  */
final case class Topology(links: Seq[Link], edges: Map[String, Edge])

/**
  * Points to an element of the project that is defined in another part of the project configuration.
  * See Link, Volume, Location.
  * @param refType type of the referenced object eg. "components", "resources"
  * @param refId
  * .  eg. "components.componentA"
  */
final case class Reference(refType: ReferenceType, refId: String) {
  def resolve(project: Project): Option[ProjectElement] = refType match {
    case ReferenceType.components => project.components.get(refId)
    case ReferenceType.resources  => project.resources.get(refId)
    case _                        => None
  }
  override def toString: String = s"$refType.$refId"
}

object Reference {
  implicit val converter: ConfigConvert[Reference] =
    ConfigConvert.viaStringOpt(
      s => {
        val referenceParts = s.split('.')
        if (referenceParts.length == 2) {
          val refType = referenceParts(0)
          val refId   = referenceParts(1)
          Some(Reference(ReferenceType.withName(refType), refId))
        } else
          throw new IllegalArgumentException(s"Failed to parse reference [$s] - it was not of format [type.id]")
      },
      _.toString
    )
}

/**
  * Kinds of references that may be used in project config.
  */
object ReferenceType extends Enumeration {
  type ReferenceType = Value
  val components, resources = Value
}

/**
  * A link between two project elements.
  *
  * @param from the ProjectElement that will be linked into the to ProjectElement
  * @param to   the ProjectElement that will be supplied the from ProjectElement
  */
final case class ResolvedLink(from: ProjectElement, to: ProjectElement)

/**
  * A link between two endpoints defined in the project.
  * An endpoint may be a resource, a component, or another addressable location.
  * E.g.
  * {
  *   from: resources.app_database
  *   to: components.public_api
  * }
  * @param from The source endpoint that will be linked from.
  * @param to The target endpoint that will be linked to.
  */
final case class Link(from: Reference, to: Reference) {
  def resolve(project: Project): ResolvedLink = {
    def invalidPathError(ref: Reference): Exception =
      new IllegalArgumentException(s"""Could not resolve link to path [$ref]. Available components [${project.components.keys.mkString(",")}]""")

    val maybeFrom = from.resolve(project)
    val maybeTo = to.resolve(project)
    (maybeFrom, maybeTo) match {
      case (None, _) => throw invalidPathError(from)
      case (_, None) => throw invalidPathError(to)
      case (Some(resolvedFrom), Some(resolvedTo)) =>
        ResolvedLink(resolvedFrom, resolvedTo)
    }
  }
}

/**
  * A link between a volume resource and a Project element.
  *
  * @param from the resource that supplies the volume.
  * @param to   the Project element that will be supplied the volume.
  */
final case class Volume(from: Resource, to: Reference)
