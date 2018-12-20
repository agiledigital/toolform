package au.com.agiledigital.toolform.command.generate.jenkins

import au.com.agiledigital.toolform.model.Project

/**
  * Properties of a build.Jenkinsfile, specifies the template and mappings of the Jenkinsfile.
  * It uses Mustache template.
  */
object BuildJenkinsfile extends Jenkinsfile {

  val templateFileName = "build.Jenkinsfile.mustache"

  def templateMappings(project: Project): Map[String, String] = {
    val prefixes = builderPrefixes(project)
    Map(
      "projectName" -> project.id,
      "label"       -> podLabel(project),
      "builds"      -> builds(project),
      "libraries"   -> jenkinsLibraries(prefixes),
      "volumes"     -> volumes(prefixes),
      "containers"  -> containers(prefixes)
    )
  }

  def podLabel(project: Project): String = s"${project.id}-build-pod"

  def builderPrefixes(project: Project): Seq[String] =
    project.components.values
      .map(_.builder)
      .toList
      .distinct
      .map(parsedBuilder)

  def jenkinsLibraries(builderPrefixes: Seq[String]): String =
    builderPrefixes
      .map(prefix => s"library '$prefix-jenkins-library'")
      .mkString("\n")

  def volumes(builderPrefixes: Seq[String]): String =
    builderPrefixes
      .map(prefix => s"""${prefix}BuilderPersistentVolumes(project: "$${project}")""")
      .mkString(",\n  ")

  def containers(builderPrefixes: Seq[String]): String =
    builderPrefixes
      .map(prefix => s"${prefix}BuilderContainerTemplate()")
      .mkString(",\n  ")

  def builds(project: Project): String = {
    val componentsByType = project.components.values
      .groupBy(
        component =>
          // Capitalize the prefix to meet the Jenkins library method convention.
          parsedBuilder(component.builder).capitalize)

    componentsByType.keys
      .map(componentType => {
        val buildComponents = componentsByType
          .get(componentType)
          .map(components => {
            components
              .map(component => {
                val subPath = settingValue("sub_path", component.settings)
                val module  = settingValue("project", component.settings)

                s"""build${componentType}Component(
                   |   baseDir: "${component.path}",
                   |   project: "$${project}",
                   |   component: "${component.id}",
                   |   buildNumber: buildNumber,
                   |   stage: buildStage
                   |   ${subPath.map(p => s""",subPath: "$p"""").getOrElse("")}
                   |   ${module.map(m => s""",module: "$m"""").getOrElse("")}
                   | )""".stripMargin
              })
              .mkString("\n")
          })
          .getOrElse("")

        s"""builds["Build $componentType Components"] = {
           |$buildComponents
           |}""".stripMargin
      })
      .mkString("\n")
  }
}
