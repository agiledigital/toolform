package au.com.agiledigital.toolform.command.inspect

import java.nio.file.Path

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.model.Project
import au.com.agiledigital.toolform.plugin.ToolFormCommandPlugin
import au.com.agiledigital.toolform.reader.ProjectReader
import com.monovore.decline._

/**
  * Prints a summary of a project definition.
  */
final class InspectCommand extends ToolFormCommandPlugin {

  override val command: Opts[Either[ToolFormError, String]] =
    Opts
      .subcommand("inspect", "Inspect and print the content of the project file") {
        Opts.option[Path]("input", short = "i", metavar = "file", help = "Input file")
      }
      .map(execute)

  def execute(inputFilePath: Path): Either[ToolFormError, String] =
    for {
      project <- ProjectReader.readProject(inputFilePath.toFile)
      summary <- summary(project)
    } yield summary

  private def summary(project: Project): Either[ToolFormError, String] = {
    val projectComponentsSummary = project.components.values.map(c => s"${c.id} ==> '${c.name}'").mkString("\n\t\t")
    val projectResourcesSummary  = project.resources.values.map(r => r.id).mkString("\n\t\t")
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
