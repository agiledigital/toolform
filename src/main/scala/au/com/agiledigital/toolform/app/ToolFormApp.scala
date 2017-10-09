package au.com.agiledigital.toolform.app

import java.io.File

import au.com.agiledigital.toolform.model._
import com.typesafe.config._
import pureconfig._
import pureconfig.error.KeyNotFound

import scala.util.{Failure, Success, Try}

/**
  * Toolform is a CLI app that generates CI/CD pipelines from a project definition.
  */
object ToolFormApp extends App {

  /**
    * Runs the tool.
    *
    * @param args Command line arguments.
    * @return Either[String, Config] - The configuration or an error.
    */
  def execute(args: Array[String]): Either[ToolFormError, String] =
    parseCommandLineArgs(args) match {
      case Some(ToolFormConfiguration(file, true)) =>
        readProject(file) match {
          case Right(project) => inspectProject(project)
          case Left(error)    => Left(error)
        }
      case Some(commandConfig) => Right("Not implemented")
      case None                => Left(ToolFormError(s"Invalid arguments - toolform failed."))
    }

  private def parseCommandLineArgs(args: Array[String]): Option[ToolFormConfiguration] = {
    val parser = new scopt.OptionParser[ToolFormConfiguration]("toolform") {
      head("toolform", "0.1")
      help("help") abbr "h" text "Displays this usage text."
      version("version") abbr "v" text "Displays version information."
      opt[File]('i', "inspect") required () valueName "<file>" action { (x, c) =>
        c.copy(in = x, inspect = true)
      } text "Displays a summary of the project definition."
    }

    parser.parse(args, ToolFormConfiguration())
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

          // Pureconfig relies on naming convention of fields in config files
          // in kebab-case to map to model member names in CamelCase.
          // Since the environment config schema has a small number of non-standard
          // field names that deviate from this convention, they need to be
          // mapped here.
          // TODO: Maybe find a better home for the field mappings, especially if the number of mappings grows.
          implicit val refConvert: ConfigConvert[Reference] = Reference.converter

          implicit val resourceFieldMapping: ProductHint[Resource] = ProductHint[Resource](new ConfigFieldMapping {
            def apply(fieldName: String): String = if (fieldName == "resourceType") "resource_type" else fieldName
          })
          implicit val componentFieldMapping: ProductHint[Component] = ProductHint[Component](new ConfigFieldMapping {
            def apply(fieldName: String): String = if (fieldName == "optionalId") "id" else fieldName
          })
          implicit val edgeFieldMapping: ProductHint[Edge] = ProductHint[Edge](new ConfigFieldMapping {
            def apply(fieldName: String): String = fieldName
          })
          implicit val subEdgeFieldMapping: ProductHint[SubEdge] = ProductHint[SubEdge](new ConfigFieldMapping {
            def apply(fieldName: String): String = if (fieldName == "edgeType") "type" else fieldName
          })
          implicit val locationFieldMapping: ProductHint[Location] = ProductHint[Location](new ConfigFieldMapping {
            def apply(fieldName: String): String = fieldName
          })
          implicit val topologyFieldMapping: ProductHint[Topology] = ProductHint[Topology](new ConfigFieldMapping {
            def apply(fieldName: String): String = fieldName
          })

          loadConfig[Project](config) match {
            case Left(failures) =>
              val failureDetails: String = failures.toList map (failure => {
                val failureMessage: String = failure.description + " @ " + failure.location.get.description
                failure match {
                  case KeyNotFound(key, location, candidates) => s"[$key] $failureMessage"
                  case _                                      => failureMessage
                }
              }) mkString ":\n"
              Left(ToolFormError(s"Failed to read project: $failureDetails"))
            case Right(project) => Right(project)
          }
        case Failure(e) =>
          Left(ToolFormError(s"Failed to parse project configuration [$projectDefinitionFile]: " + e.getMessage))
      }
    }

  private def inspectProject(project: Project): Either[ToolFormError, String] =
    Right(
      s"Project: [${project.name}]\n" +
        s"\tComponents:\n\t\t${project.components.values map (c => s"${c.id} ==> '${c.name}'") mkString "\n\t\t"}\n" +
        s"\tResources:\n\t\t${project.resources.values map (r => r.id) mkString "\n\t\t"}\n" +
        s"\tLinks:\n\t\t${project.topology.links map (l => s"${l.from.ref} -> ${l.to.ref}") mkString "\n\t\t"}\n")

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
case class ToolFormError(message: String)
