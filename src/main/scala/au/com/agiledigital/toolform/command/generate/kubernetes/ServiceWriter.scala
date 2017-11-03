package au.com.agiledigital.toolform.command.generate.kubernetes

import au.com.agiledigital.toolform.model.{PortMapping, ToolFormService}
import cats.implicits._

/**
  * Writes Kubernetes service definitions.
  *
  * Not to be confused with the Service trait defined in the toolform model.
  */
object ServiceWriter extends KubernetesWriter {

  private def writeSelector(service: ToolFormService): Result[Unit] = {
    val selectorEntry = determineSelectorEntry(service)
    for {
      _ <- write("selector:")
      _ <- indented {
            for {
              _ <- write(selectorEntry)
            } yield ()
          }
    } yield ()
  }

  private def writePortEntry(portMapping: PortMapping): Result[Unit] = {
    val port       = portMapping.port
    val targetPort = portMapping.targetPort
    val protocol   = portMapping.protocol.toString.toUpperCase
    for {
      _ <- write(s"-")
      _ <- indented {
            for {
              _ <- write(s"name: \042port-$port\042")
              _ <- write(s"port: $port")
              _ <- write(s"targetPort: $targetPort")
              _ <- write(s"protocol: $protocol")
            } yield ()
          }
    } yield ()
  }

  private def writePorts(allPorts: List[PortMapping]): Result[Unit] =
    if (allPorts.nonEmpty) {
      for {
        _ <- write("ports:")
        _ <- allPorts.traverse_(writePortEntry)
      } yield ()
    } else {
      write("clusterIP: \"None\"")
    }

  /**
    * Writes a Kubernetes "service" specification based on the provided toolform service.
    *
    * A service is an abstraction to provide a stable interface to one or more pods.
    * It specifies the DNS the name used to access a deployment.
    *
    * For example, an update could be deployed which means a pod running an old version of a server gets destroyed and
    * a new pod with the new version of the server is created. Although the deployment has changed, the service remains static
    * and dependent services don't need to be given a new address to access the new pod.
    *
    * @see https://kubernetes.io/docs/concepts/services-networking/service/
    *
    * @param service    the service object that will be used to create the deployment spec.
    * @return           a state monad encapsulating the context of the writing process after the method has completed.
    */
  def writeService(service: ToolFormService): Result[Unit] = {
    val serviceName = determineServiceName(service)
    val allPorts    = service.externalPorts ++ service.exposedPorts
    val nodeType    = if (service.externalPorts.nonEmpty) "NodePort" else "ClusterIP"

    for {
      _ <- write("---")
      _ <- write("apiVersion: v1")
      _ <- write("kind: Service")
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
              _ <- write(s"type: $nodeType")
              _ <- writeSelector(service)
              _ <- writePorts(allPorts)
            } yield ()
          }
    } yield ()
  }
}
