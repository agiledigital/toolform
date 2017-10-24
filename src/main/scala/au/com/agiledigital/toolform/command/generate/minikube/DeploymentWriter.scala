package au.com.agiledigital.toolform.command.generate.minikube

import au.com.agiledigital.toolform.model.ToolFormService
import cats.implicits._

object DeploymentWriter extends KubernetesWriter {

  private def writeTemplateMetadata(service: ToolFormService): Result[Unit] = {
    val selectorEntry = determineSelectorEntry(service)
    for {
      _ <- write("metadata:")
      _ <- indented {
        for {
          _ <- write("labels:")
          _ <- indented {
            for {
              _ <- write(selectorEntry)
            } yield ()
          }
        } yield ()
      }
    } yield ()
  }

  private def writeEnvironmentVariable(entry: (String, String)): Result[Unit] = {
    val (name, value) = entry
    for {
      _ <- write("-")
      _ <- indented {
        for {
          _ <- write(s"name: $name")
          _ <- write(s"value: $value")
        } yield ()
      }
    } yield ()
  }

  private def writeEnvironmentVariables(service: ToolFormService): Result[Unit] =
    if (service.environment.nonEmpty) {
      for {
        _ <- write("env:")
        _ <- indented {
          for {
            _ <- service.environment.toList
              .traverse_((entry) => writeEnvironmentVariable(entry))
          } yield ()
        }
      } yield ()
    } else {
      identity
    }

  private def writeContainerPorts(containerPorts: List[Int]): Result[Unit] =
    if (containerPorts.nonEmpty) {
      for {
        _ <- write("ports:")
        _ <- indented {
          for {
            _ <- containerPorts.traverse_(containerPort => write(s"- containerPort: $containerPort"))
          } yield ()
        }
      } yield ()
    } else {
      identity
    }

  private def writeContainer(projectId: String, service: ToolFormService): Result[Unit] = {
    val imageName = determineImageName(projectId, service)
    val serviceName = determineServiceName(service)
    val containerPorts = (service.externalPorts ++ service.exposedPorts)
      .map((portMapping) => portMapping.targetPort)

    for {
      _ <- write("containers:")
      _ <- write("-")
      _ <- indented {
        for {
          _ <- write(s"image: $imageName")
          _ <- write(s"name: $serviceName")
          _ <- write("imagePullPolicy: IfNotPresent") // Makes locally built images work
          _ <- writeEnvironmentVariables(service)
          _ <- writeContainerPorts(containerPorts)
        } yield ()
      }
    } yield ()
  }

  /**
    * Writes a Kubernetes "deployment" specification based on the provided toolform service.
    *
    * A deployment is an abstraction to control the creation of pods.
    * It defines what image to pull and how many replicas to create.
    *
    *
    * @see https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
    *
    * @param projectId  the ID of the project object which is used to generate certain names,
    * @param service    the service object that will be used to create the deployment spec.
    * @return           a state monad encapsulating the context of the writing process after the method has completed.
    */
  def writeDeployment(projectId: String, service: ToolFormService): Result[Unit] = {
    val serviceName = determineServiceName(service)

    for {
      _ <- write("---")
      _ <- write("apiVersion: extensions/v1beta1")
      _ <- write("kind: Deployment")
      _ <- write("metadata:")
      _ <- indented {
        for {
          _ <- writeAnnotations(service)
          _ <- write(s"name: $serviceName")
        } yield ()
      }
      _ <- write("spec:")
      _ <- indented {
        for {
          _ <- write(s"replicas: 1")
          _ <- write(s"template:")
          _ <- indented {
            for {
              _ <- writeTemplateMetadata(service)
              _ <- write(s"spec:")
              _ <- indented {
                for {
                  _ <- writeContainer(projectId, service)
                } yield ()
              }
            } yield ()
          }
        } yield ()
      }
    } yield ()
  }
}
