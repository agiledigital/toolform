package au.com.agiledigital.toolform.app

import java.io.File
import java.util.ServiceLoader

import au.com.agiledigital.toolform.model._
import au.com.agiledigital.toolform.plugin.ToolFormPlugin
import au.com.agiledigital.toolform.version.BuildInfo
import au.com.agiledigital.toolform.tasks.InspectTask
import au.com.agiledigital.toolform.tasks.generate.GenerateTask
import com.typesafe.config._
import enumeratum.{Enum, EnumEntry}
import pureconfig._
import pureconfig.error.{ConfigReaderFailures, KeyNotFound}
import scopt._

import scala.collection.JavaConverters._
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
  def execute(args: Array[String]): Either[ToolFormError, String] = {
    val plugins = loadPlugins
    for {
      toolConfiguration <- parseCommandLineArgs(args, plugins)
      project           <- readProject(toolConfiguration.in)
      plugin            <- toolConfiguration.activePlugin
      command           <- plugin.configureCommand(toolConfiguration)
      result            <- command.execute(project)
    } yield result
  }

  private def loadPlugins: List[ToolFormPlugin] =
    ServiceLoader.load(classOf[ToolFormPlugin]).asScala.toList

  private def parseCommandLineArgs(args: Array[String], plugins: List[ToolFormPlugin]): Either[ToolFormError, ToolFormConfiguration] = {
    val parser = new OptionParser[ToolFormConfiguration](BuildInfo.name) {
      head(BuildInfo.name, BuildInfo.version)
      help("help").abbr("h").text("Displays this usage text.")
      version("version").abbr("v").text("Displays version information.")
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
      val failureDetails: String = failures.toList
        .map(failure => {
          val locationDescription = failure.location.map(_.description).getOrElse("Unknown location")
          val failureMessage: String = failure.description + " @ " + locationDescription
          failure match {
            case KeyNotFound(key, _, _) => s"[$key] $failureMessage"
            case _                      => failureMessage
          }
        })
        .mkString(":\n")
      ToolFormError(s"Failed to read project: [$failureDetails]")
    })

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
  * @param activePlugin The plugin to be executed.
  */
final case class ToolFormConfiguration(in: File = new File("."), out: File = new File("."), activePlugin: Either[ToolFormError, ToolFormPlugin] = Left(ToolFormError("Command not specified")))

/**
  * A simple error type for the toolform CLI app.
  *
  * @param message The error detail message.
  */
final case class ToolFormError(message: String)

/**
  * An enumeration representing all the modes this tool can function in.
  */
sealed trait ToolFormAppMode extends EnumEntry

object ToolFormAppMode extends Enum[ToolFormAppMode] {
  val values: IndexedSeq[ToolFormAppMode] = findValues

  case object None     extends ToolFormAppMode
  case object Inspect  extends ToolFormAppMode
  case object Generate extends ToolFormAppMode
}
