package au.com.agiledigital.toolform.plugin

import java.io.File

import au.com.agiledigital.toolform.app.{ToolFormConfiguration, ToolFormError}
import au.com.agiledigital.toolform.model.Project
import scopt.OptionParser

final class InspectPlugin extends ToolFormPlugin {
  override def commandName = "inspect"

  override def commandVersion = "0.0.1"

  override def commandLineArgs(parser: OptionParser[ToolFormConfiguration]): Unit = {
    val plugin = this
    parser
      .opt[File]('i', commandName)
      .required()
      .valueName("<file>")
      .action { (x, c) =>
        c.copy(in = x, activePlugin = Right(plugin))
      }
      .text("Displays a summary of the project definition.")
  }

  override def configureCommand(toolConfig: ToolFormConfiguration) = Right(new InspectCommand(toolConfig))
}

final class InspectCommand(toolFormConfiguration: ToolFormConfiguration) extends ToolFormCommand {

  override def execute(project: Project): Either[ToolFormError, String] = {
    val projectComponentsSummary = project.components.values.map(c => s"${c.id} ==> '${c.name}'").mkString("\n\t\t")
    val projectResourcesSummary = project.resources.values.map(r => r.id).mkString("\n\t\t")
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
