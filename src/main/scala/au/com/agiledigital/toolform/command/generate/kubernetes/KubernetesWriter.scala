package au.com.agiledigital.toolform.command.generate.kubernetes

import au.com.agiledigital.toolform.command.generate.Formatting.{componentImageName, componentServiceName}
import au.com.agiledigital.toolform.command.generate.YamlWriter
import au.com.agiledigital.toolform.model.{Component, Resource, ToolFormService}

/**
  * Provides functionality that is common between "Deployment" and "Service" specifications.
  */
trait KubernetesWriter extends YamlWriter {

  def determineServiceName(service: ToolFormService): String =
    service match {
      case component: Component => componentServiceName(component)
      case resource: Resource   => resource.id
    }

  def determineImageName(projectId: String, service: ToolFormService): String =
    service match {
      case component: Component => componentImageName(projectId, component)
      case resource: Resource   => resource.image.getOrElse("<missing image>")
    }

  def determineSelectorEntry(service: ToolFormService): String = {
    val serviceName = determineServiceName(service)
    service match {
      case _: Component => s"component: $serviceName"
      case _: Resource  => s"resource: $serviceName"
    }
  }

  def writeAnnotations(service: ToolFormService): Result[Unit] = service match {
    case component: Component =>
      for {
        _ <- write("annotations:")
        _ <- indented {
              for {
                _ <- write(s"source.path: \042${component.path}\042")
                _ <- write("project.artefact: \"true\"")
              } yield ()
            }
      } yield ()
    case _ => identity
  }
}
