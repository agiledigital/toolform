package au.com.agiledigital.toolform.command.generate.jenkins

import au.com.agiledigital.toolform.model.Project
import cats.implicits._

/**
  * Properties of a deploy.Jenkinsfile, specifies the template and mappings of the Jenkinsfile.
  * It uses Mustache template.
  */
object DeployJenkinsfile extends Jenkinsfile {

  val templateFileName = "deploy.Jenkinsfile.mustache"

  def templateMappings(project: Project): Map[String, String] =
    Map(
      "projectName" -> project.id,
      "components"  -> components(project)
    )

  def components(project: Project): String =
    project.components.values
      .map(component => {
        val builder = parsedBuilder(component.builder)
        val runner = if (builder === "docker") {
          None
        } else {
          Some(builder)
        }
        s"""[component: "${component.id}" ${runner.map(r => s""", runner: "$r"""").getOrElse("")} ]"""
      })
      .mkString(",\n")
}
