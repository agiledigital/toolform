package au.com.agiledigital.toolform.command.generate.docker

import au.com.agiledigital.toolform.model.PortMapping

/**
  * A collection of pure formatting functions for use for Docker Compose file generation.
  */
object DockerFormatting {

  def formatEnvironment(entry: (String, String)): String = s"- ${entry._1}=${entry._2}"

  def formatPort(port: PortMapping): String = {
    val portString = port.toPortString
    s"- \042$portString\042"
  }
}
