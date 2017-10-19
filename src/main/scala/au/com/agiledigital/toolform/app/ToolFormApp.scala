package au.com.agiledigital.toolform.app

import java.io.File

import au.com.agiledigital.toolform.model._
import au.com.agiledigital.toolform.tasks.generate.{GenerateTask, GenerateTaskConfiguration, GenerateTaskOutputType}
import au.com.agiledigital.toolform.version.BuildInfo
import au.com.agiledigital.toolform.tasks.InspectTask
import au.com.agiledigital.toolform.tasks.generate.GenerateTask
import com.typesafe.config._
import enumeratum.{Enum, EnumEntry}
import pureconfig._
import pureconfig.error.{ConfigReaderFailures, KeyNotFound}

import scala.collection.immutable.IndexedSeq
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
      result <- toolConfiguration.mode match {
        case ToolFormAppMode.Inspect  => new InspectTask().run(toolConfiguration, project)
        case ToolFormAppMode.Generate => new GenerateTask().run(toolConfiguration, project)
        case _                        => Left(ToolFormError("Cannot determine app mode"))
      }
    } yield result

  private def parseCommandLineArgs(args: Array[String]): Either[ToolFormError, ToolFormConfiguration] = {
    val parser = new scopt.OptionParser[ToolFormConfiguration](BuildInfo.name) {
      head(BuildInfo.name, BuildInfo.version)
      help("help").abbr("h").text("Displays this usage text.")
      version("version").abbr("v").text("Displays version information.")

      cmd("inspect")
        .action((_, c) => c.copy(mode = ToolFormAppMode.Inspect))
        .text("displays a summary of the project definition.")
        .children(
          opt[File]('i', "in-file") required () valueName "<file>" action { (x, c) =>
            c.copy(in = x)
          } text "the path to the project file to inspect"
        )

      cmd("generate")
        .action((_, c) => c.copy(mode = ToolFormAppMode.Generate))
        .text("generates config files for container orchestration.")
        .children(
          opt[File]('i', "in-file") required () valueName "<file>" action { (x, c) =>
            c.copy(in = x)
          } text "the path to the project file to inspect",
          opt[File]('o', "out-file") required () valueName "<file>" action { (x, c) =>
            c.copy(generateTaskConfiguration = c.generateTaskConfiguration.copy(out = x))
          } text "the path to output the generated file/s",
          opt[Unit]('d', "generate-docker-compose")
            .action((_, c) => c.copy(generateTaskConfiguration = c.generateTaskConfiguration.copy(generateTaskOutputType = GenerateTaskOutputType.DockerComposeV3)))
            .text("generate a Docker Compose v3 file as output (default)")
        )
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
          val locationDescription    = failure.location.map(_.description).getOrElse("Unknown location")
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
  * @param mode The mode the tool should run in.
  * @param generateTaskConfiguration The configuration used by the "Generate" task.
  */
final case class ToolFormConfiguration(in: File = new File("."), mode: ToolFormAppMode = ToolFormAppMode.None, generateTaskConfiguration: GenerateTaskConfiguration = GenerateTaskConfiguration())

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
