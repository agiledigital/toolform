package au.com.agiledigital.toolform.reader

import java.io.File

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.model.{Component, Endpoint, Project}
import cats.data.Validated
import cats.data.Validated.{invalid, valid}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.error.{ConfigReaderFailures, KeyNotFound}
import pureconfig.module.enumeratum._
import cats.implicits._
import cats.data.NonEmptyList

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
  def readProject(projectDefinitionFile: File): Either[NonEmptyList[ToolFormError], Project] =
    if (!projectDefinitionFile.exists()) {
      Left(NonEmptyList.of(ToolFormError(s"File [$projectDefinitionFile] does not exist.")))
    } else if (!projectDefinitionFile.isFile) {
      Left(NonEmptyList.of(ToolFormError(s"File [$projectDefinitionFile] is not a file.")))
    } else {
      // Read in the root configuration file.
      Try(ConfigFactory.parseFile(projectDefinitionFile.getAbsoluteFile)) match {
        case Success(simpleConfig) =>
          // Resolve the configuration aka. replace variable substitutions.
          val config = simpleConfig.resolve()

          val projectResult = loadConfig[Project](config)

          for {
            collectedReadErrors    <- resultOrCollectReadErrors(projectResult)
            validatedProjectResult <- validatedProject(collectedReadErrors)
          } yield validatedProjectResult

        case Failure(e) =>
          Left(NonEmptyList.of(ToolFormError(s"Failed to parse project configuration [$projectDefinitionFile]: [${e.getMessage}]")))
      }
    }

  private def validatedProject(project: Project): Either[NonEmptyList[ToolFormError], Project] =
    project.topology.endpoints.toList
      .traverseU {
        case (endpointId, endpoint) =>
          validatedEndpoint(endpointId, endpoint, project.components)
      }
      .map { _ =>
        project
      }
      .toEither

  private def validatedEndpoint(endpointId: String, endpoint: Endpoint, components: Map[String, Component]): Validated[NonEmptyList[ToolFormError], Endpoint] = {
    val targetComponent = components.get(endpoint.target)
    val targetPort      = endpoint.portMapping.targetPort

    targetComponent match {
      case Some(component) if portIsValid(component, targetPort) =>
        valid(endpoint)
      case Some(_) =>
        invalid(NonEmptyList.of(ToolFormError(s"Endpoint [$endpointId] targets invalid port [$targetPort] on component id [${endpoint.target}]")))
      case _ =>
        invalid(NonEmptyList.of(ToolFormError(s"Endpoint [$endpointId] targets invalid component id [${endpoint.target}]")))
    }
  }

  private def portIsValid(component: Component, targetPort: Int) = component.exposedPorts.map { _.port }.exists { _ === targetPort }

  private def resultOrCollectReadErrors(projectResult: Either[ConfigReaderFailures, Project]): Either[NonEmptyList[ToolFormError], Project] =
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
      NonEmptyList.of(ToolFormError(s"Failed to read project: [$failureDetails]"))
    })
}
