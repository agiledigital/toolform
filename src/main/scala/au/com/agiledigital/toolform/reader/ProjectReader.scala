package au.com.agiledigital.toolform.reader

import java.io.File

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.model.Project
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.error.{ConfigReaderFailures, KeyNotFound}
import pureconfig.module.enumeratum._

import scala.util.{Failure, Success, Try}

/**
  * Reads a HOCON project definition file and maps it to the [[Project]] object model.
  */
object ProjectReader {

  /**
    * Reads a project definition file and maps it to the [[Project]] object model.
    * @param projectDefinitionFile The HOCON project definition file.
    * @return Either a Project or an error.
    */
  def readProject(projectDefinitionFile: File): Either[ToolFormError, Project] =
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
}
