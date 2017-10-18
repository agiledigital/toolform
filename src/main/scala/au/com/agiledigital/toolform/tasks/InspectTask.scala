package au.com.agiledigital.toolform.tasks

import au.com.agiledigital.toolform.app.{ToolFormConfiguration, ToolFormError}
import au.com.agiledigital.toolform.model.Project

class InspectTask() extends Task {

  override def run(toolFormConfiguration: ToolFormConfiguration, project: Project): Either[ToolFormError, String] = {
    val projectComponentsSummary = project.sortedComponents.values.map(c => s"${c.id} ==> '${c.name}'").mkString("\n\t\t")
    val projectResourcesSummary = project.sortedResources.values.map(r => r.id).mkString("\n\t\t")
    val projectLinksSummary = project.topology.links
      .map(l => {
        val resolvedLink = l.resolve(project)
        s"${resolvedLink.from.id} -> ${resolvedLink.to.id}"
      })
      .mkString("\n\t\t")
    Right(
      s"Project: [${project.name}]\n" +
        s"\tComponents:\n\t\t$projectComponentsSummary\n" +
        s"\tResources:\n\t\t$projectResourcesSummary\n" +
        s"\tLinks:\n\t\t$projectLinksSummary\n")
  }
}
