package au.com.agiledigital.toolform.command.generate.jenkins

import au.com.agiledigital.toolform.model.Project
import com.typesafe.config.Config

/**
  * Provides the properties that are needed to generate a Jenkinsfile from a provided template.
  */
trait Jenkinsfile {

  /**
    * Name of the Jenkinsfile template.
    * It should be a Mustache template.
    */
  val templateFileName: String

  /**
    * Provides the mappings of the values for the template.
    * @param project configurations of the project.
    * @return mappings that can be passed into the template for value substitution.
    */
  def templateMappings(project: Project): Map[String, String]

  /**
    * Parses a builder name, this is specified in the project.conf file for component, e.g.
    *  "public_api": {
    *     ...
    *     builder: "play25",
    *     ...
    *   }
    * We may use this builder name to locate a certain Jenkins library, e.g. play25-jenkins-library.
    * The name of the builder has to be specific and it should not be a docker image URI.
    * There are some component definitions in project.conf using docker build as:
    *  "elastic_search": {
    *     ...
    *     builder: "docker.agiledigital.com.au:5000/agile/docker-build:201708061841",
    *     ...
    *  }
    * But the Jenkins library prefix should be "docker" (docker-jenkins-library), so we make sure it returns the
    * "docker" as builder name instead.
    * @param builder from the project.conf to be parsed.
    * @return parsed builder prefix.
    */
  def parsedBuilder(builder: String): String =
    if (builder.contains("docker-build")) {
      "docker"
    } else {
      builder
    }

  /**
    * Value of a setting based on the path provided.
    * @param path in the setting configuration.
    * @param settings used to retrieve the path.
    * @return the setting value if found, or none.
    */
  def settingValue(path: String, settings: Option[Config]): Option[String] =
    settings.flatMap(config => {
      if (config.hasPath(path)) {
        Some(config.getString(path))
      } else {
        None
      }
    })
}
