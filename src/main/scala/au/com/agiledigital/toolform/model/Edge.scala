package au.com.agiledigital.toolform.model

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
  *     subEdges {
  *       public {
  *         type: http
  *         dnsPrefix: public.qhopper
  *         locations: [{
  *             location: /qhopper-api
  *             targetLocation: /
  *             target: components.public_api
  *             targetName: PUBLIC_API
  *             targetPort: 9000
  *           },{
  *             location: /
  *             target: components.public_www
  *             targetName: PUBLIC_WWW
  *             targetPort: 8000
  *           }
  *         ]
  *       },
  *       admin {
  *         dnsPrefix: admin.qhopper
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
  * @param path     the full path to the Edge in the configuration.
  * @param name     the name of the edge.
  * @param subEdges the SubEdges that are a part of the Edge.
  */
case class Edge( /*path: String, name: String,*/ subEdges: Map[String, SubEdge] /*, project: Project*/ ) extends ProjectElement {

  /**
    * The id of this element.
    *
    * @return the unique identifier of the element.
    */
  override def id: String = "TODO: edge" // name + "_nginx"
}

/**
  * A SubEdge. See [[Edge]] for more information.
  *
  * @param path        the full path to the Edge in the config.
  * @param name        the name of the sub edge.
  * @param edgeName    the name of the edge that this is a part of.
  * @param edgeType    the type of the subedge.
  * @param edgeBuilder the builder that is used to build the edge.
  * @param dnsPrefix   the dns prefix for this subedge.
  * @param locations   the Locations in this subedge.
  * @param project     the project that this subedge is a part of.
  */
case class SubEdge( /*path: String, name: String, edgeName: String,*/ edgeType: String,
                   edgeBuilder: Option[String],
                   dnsPrefix: String,
                   externalUrl: String,
                   locations: Seq[Location] /*, project: Project*/ )
    extends ProjectElement {

  override def id: String = "TODO: subedge" //s"${edgeName}_${name}_nginx"

}

/**
  * A Location within a SubEdge.
  *
  * @param path           the full path to the location in the config.
  * @param location       the external path of the location.
  * @param targetLocation the internal path of the location.
  * @param target         the target Component or Resource.
  * @param targetName     the name of the target.
  * @param targetPort     the internal port of the location.
  */
case class Location( /*path: String,*/ location: Option[String], targetLocation: Option[String], target: Reference, targetName: String, targetPort: Int)
