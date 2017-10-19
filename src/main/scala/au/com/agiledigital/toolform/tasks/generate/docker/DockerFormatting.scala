package au.com.agiledigital.toolform.tasks.generate.docker

import au.com.agiledigital.toolform.model.{Component, Resource, SubEdgeType}

object DockerFormatting {
  def normaliseServiceName(name: String): String =
    name
      .replace("/", "")
      .replace(" ", "")
      .replace("-", "")
      .replace("_", "")
      .toLowerCase

  def normaliseImageName(name: String): String =
    name
      .replace(" ", "_")
      .replace("-", "_")
      .toLowerCase

  def componentServiceName(component: Component): String =
    normaliseServiceName(s"${component.id}")

  def resourceServiceName(resource: Resource): String =
    normaliseServiceName(s"${resource.id}")

  def subEdgeServiceName(projectId: String, subEdgeDef: SubEdgeDef): String =
    normaliseServiceName(s"${projectId}${subEdgeDef.edgeId}${subEdgeDef.subEdgeId}nginx")

  def componentImageName(projectId: String, component: Component): String =
    normaliseImageName(s"${projectId}/${component.id}")

  def resourceImageName(resource: Resource): String =
    normaliseImageName(s"${resource.image}")

  def subEdgeImageName(projectId: String, subEdgeDef: SubEdgeDef): String =
    normaliseImageName(s"${projectId}_${subEdgeDef.edgeId}_${subEdgeDef.subEdgeId}_nginx")

  def subEdgePortDefinition(subEdgeDef: SubEdgeDef): String =
    SubEdgeType.withNameInsensitive(subEdgeDef.subEdge.edgeType) match {
      case SubEdgeType.http  => "- \"80:80\""
      case SubEdgeType.https => "- \"443:443\""
    }

  def formatEnvironment(entry: (String, String)): String = s"- ${entry._1}=${entry._2}"

  def formatPort(port: String): String = s"- \042$port\042"
}
