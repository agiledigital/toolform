package au.com.agiledigital.toolform.app

import java.io.File

import au.com.agiledigital.toolform.model._
import com.typesafe.config._
import pureconfig._
import pureconfig.error.{ConfigReaderFailures, KeyNotFound}

import scala.util.{Failure, Success, Try}

/**
  * A tool to generate CI/CD pipelines from a project definition.
  */
object ToolFormApp extends App {

  /**
    * Runs the tool.
    *
    * @param args Command line arguments.
    * @return Either[String, Config] - The configuration or an error.
    */
  def execute(args: Array[String]): Either[ToolFormError, String] =
    for {
      toolConfiguration <- parseCommandLineArgs(args)
      project           <- readProject(toolConfiguration.in)
      summary           <- projectSummary(project)
    } yield summary

  private def parseCommandLineArgs(args: Array[String]): Either[ToolFormError, ToolFormConfiguration] = {
    val parser = new scopt.OptionParser[ToolFormConfiguration]("toolform") {
      head("toolform", "0.1")
      help("help").abbr("h").text("Displays this usage text.")
      version("version").abbr("v").text("Displays version information.")
      opt[File]('i', "inspect") required () valueName "<file>" action { (x, c) =>
        c.copy(in = x, inspect = true)
      } text "Displays a summary of the project definition."
    }

    parser.parse(args, ToolFormConfiguration()).toRight(ToolFormError(s"Invalid arguments - toolform failed."))
  }

  private def readProject(projectDefinitionFile: File): Either[ToolFormError, Project] =
    if (!projectDefinitionFile.exists()) {
      Left(ToolFormError(s"File [$projectDefinitionFile] does not exist."))
    } else if (!projectDefinitionFile.isFile) {
      Left(ToolFormError(s"File [$projectDefinitionFile] is not a file."))
    } else {
      // Read in the root configuration file.
      Try(ConfigFactory.parseFile(projectDefinitionFile)) match {
        case Success(simpleConfig) =>
          // Resolve the configuration aka. replace variable substitutions.
          val config = simpleConfig.resolve()

          val projectResult = loadConfig[Project](config)
          resultOrCollectReadErrors(projectResult)
        case Failure(e) =>
          Left(ToolFormError(s"Failed to parse project configuration [$projectDefinitionFile]: [${e.getMessage}]"))
      }
    }

  private def resultOrCollectReadErrors(projectResult: Either[ConfigReaderFailures, Project]): Either[ToolFormError, Project] =
    projectResult.left.map(failures => {
      val failureDetails: String = failures.toList.map(failure => {
        val locationDescription = failure.location.map(_.description).getOrElse("Unknown location")
        val failureMessage: String = failure.description + " @ " + locationDescription
        failure match {
          case KeyNotFound(key, _, _) => s"[$key] $failureMessage"
          case _                      => failureMessage
        }
      }).mkString(":\n")
      ToolFormError(s"Failed to read project: [$failureDetails]")
    })

  private def projectSummary(project: Project): Either[ToolFormError, String] = {
    val projectComponentsSummary = project.components.values.map(c => s"${c.id} ==> '${c.name}'").mkString("\n\t\t")
    val projectResourcesSummary = project.resources.values.map(r => r.id).mkString("\n\t\t")
    val projectLinksSummary = project.topology.links.map(l => {
      val resolvedLink = l.resolve(project)
      s"${resolvedLink.from.id} -> ${resolvedLink.to.id}"
    }).mkString("\n\t\t")
    Right(
      s"Project: [${project.name}]\n" +
      s"\tComponents:\n\t\t$projectComponentsSummary\n" +
      s"\tResources:\n\t\t$projectResourcesSummary\n" +
      s"\tLinks:\n\t\t$projectLinksSummary\n")
  }

  execute(args) match {
    case Left(error) =>
      Console.err.println(error.message)
      sys.exit(1)
    case Right(result) =>
      println(result)
  }
}

/**
  * The configuration for the tool.
  *
  * @param in The name of the file to be processed.
  * @param inspect Is this an inspect command?
  */
final case class ToolFormConfiguration(in: File = new File("."), inspect: Boolean = false)

/**
  * A simple error type for the toolform CLI app.
  *
  * @param message The error detail message.
  */
final case class ToolFormError(message: String)
