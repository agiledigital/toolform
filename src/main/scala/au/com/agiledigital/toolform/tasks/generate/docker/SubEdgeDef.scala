package au.com.agiledigital.toolform.tasks.generate.docker

import au.com.agiledigital.toolform.model.{Project, SubEdge}

/**
  * An object that collects relevant data on a sub edge.
  *
  * This was created because the Edge and SubEdge objects don't seem to have a way of knowing their own ID.
  * The ID is specified as the key of the dictionary they are defined in so they have no access to it.
  *
  * E.g.
  * "some_id": {
  *   "object_key": "object_value"
  * }
  *
  * The object defined by the key "some_id" has no way of knowing it's ID is "some_id".
  * Therefore this context must be constructed by iterating the maps and collecting the data into this object.
  *
  * We could put the ID in the object itself but this would add redundancy.
  *
  * @param edgeId     The ID of the parent edge of the wrapped subedge.
  * @param subEdgeId  The ID of the wrapped subedge.
  * @param subEdge    The subedge that is wrapped by this object.
  */
final case class SubEdgeDef(edgeId: String, subEdgeId: String, subEdge: SubEdge)

object SubEdgeDef {

  /**
    * Generates a list of SubEdgeDef objects from a project.
    *
    * @param project the project to extract the information from.
    * @return a iterable of SubEdgeDef objects.
    */
  def subEdgeDefsFromProject(project: Project): Iterable[SubEdgeDef] =
    for {
      (edgeName, edge)       <- project.topology.sortedEdges
      (subEdgeName, subEdge) <- edge.sortedSubEdges
    } yield SubEdgeDef(edgeName, subEdgeName, subEdge)

}
