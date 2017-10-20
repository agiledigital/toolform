package au.com.agiledigital.toolform.command.generate.docker

import au.com.agiledigital.toolform.command.generate.Formatting.normaliseImageName
import au.com.agiledigital.toolform.model.{PortMapping, SubEdgeType}

/**
  * A collection of pure formatting functions for use for Docker Compose file generation.
  */
object DockerFormatting {

  def formatEnvironment(entry: (String, String)): String = s"- ${entry._1}=${entry._2}"

  def formatPort(port: PortMapping): String = {
    val portString = port.toPortString
    s"- \042$portString\042"
  }

  def subEdgeImageName(projectId: String, subEdgeDef: SubEdgeDef): String =
    normaliseImageName(s"${projectId}_${subEdgeDef.edgeId}_${subEdgeDef.subEdgeId}_nginx")

  def subEdgePortDefinition(subEdgeDef: SubEdgeDef): PortMapping =
    SubEdgeType.withNameInsensitive(subEdgeDef.subEdge.edgeType) match {
      case SubEdgeType.Http  => PortMapping(80, 80)
      case SubEdgeType.Https => PortMapping(443, 443)
    }
}
