package au.com.agiledigital.toolform.model

import enumeratum.{Enum, EnumEntry}
import pureconfig.{CamelCase, ConfigFieldMapping, KebabCase, ProductHint}

import scala.collection.SortedMap
import scala.collection.immutable.{IndexedSeq, TreeMap}

/**
  * An Edge makes some of the project Components and Resources available externally.
  *
  * An Edge consists of several SubEdges. Each SubEdge routes to several Locations. A Location
  * points to a Component (or Resource) and provides the routing information.
  *
  * E.g.
  * {{{
  *
  * edges {
  *   main {
  *     sub-edges {
  *       public {
  *         type: http
  *         dns-prefix: public.qhopper
  *         locations: [{
  *             location: /qhopper-api
  *             target-location: /
  *             target: components.public_api
  *             target-name: PUBLIC_API
  *             target-port: 9000
  *           },{
  *             location: /
  *             target: components.public_www
  *             target-name: PUBLIC_WWW
  *             target-port: 8000
  *           }
  *         ]
  *       },
  *       admin {
  *         dns-prefix: admin.qhopper
  *       }
  *     }
  *   }
  * }
  *
  * }}}
  *
  * Will create an edge named 'main' it will use hostname prefixes to route to the public and admin subedges.
  * The public subedge melds the public-api and public-www components together.
  *
  * @param subEdges the SubEdges that are a part of the Edge.
  */
final case class Edge(subEdges: Map[String, SubEdge]) extends ProjectElement {

  /**
    * The id of this element.
    *
    * @return the unique identifier of the element.
    */
  override def id: String = "TODO: edge id" // name + "_nginx"

  val sortedSubEdges: SortedMap[String, SubEdge] = TreeMap(subEdges.toArray: _*)
}

/**
  * A SubEdge. See [[Edge]] for more information.
  *
  * @param edgeType    the type of the subedge.
  * @param edgeBuilder the builder that is used to build the edge.
  * @param dnsPrefix   the dns prefix for this subedge.
  * @param locations   the Locations in this subedge.
  */
final case class SubEdge(edgeType: String, edgeBuilder: Option[String], dnsPrefix: String, externalUrl: String, locations: Seq[Location]) extends ProjectElement {

  override def id: String = "TODO: subedge id" //s"${edgeName}_${name}_nginx"

}

object SubEdge {
  implicit val fieldMapping: ProductHint[SubEdge] =
    ProductHint[SubEdge](ConfigFieldMapping(CamelCase, KebabCase).withOverrides("edgeType" -> "type"))

}

/**
  * A Location within a SubEdge.
  *
  * @param location       the external path of the location.
  * @param targetLocation the internal path of the location.
  * @param target         the target Component or Resource.
  * @param targetName     the name of the target.
  * @param targetPort     the internal port of the location.
  */
final case class Location(location: Option[String], targetLocation: Option[String], target: Reference, targetName: String, targetPort: Int)

/**
  * An enumeration representing types a subedge can be.
  */
sealed trait SubEdgeType extends EnumEntry

object SubEdgeType extends Enum[SubEdgeType] {
  val values: IndexedSeq[SubEdgeType] = findValues

  case object Http  extends SubEdgeType
  case object Https extends SubEdgeType

}
