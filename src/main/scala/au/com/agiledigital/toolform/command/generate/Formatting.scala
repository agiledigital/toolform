package au.com.agiledigital.toolform.command.generate

import au.com.agiledigital.toolform.model.Component

/**
  * A collection of pure formatting functions for use by the Generate task.
  */
object Formatting {

  def normaliseServiceId(name: String): String =
    name
      .replace("/", "-")
      .replace(" ", "-")
      .replace("_", "-")
      .toLowerCase

  def normaliseImageName(name: String): String =
    name
      .replace(" ", "_")
      .replace("-", "_")
      .toLowerCase

  def componentServiceName(component: Component): String =
    normaliseServiceId(component.id)

  def componentImageName(projectId: String, component: Component): String =
    normaliseImageName(s"$projectId/${component.id}")
}
