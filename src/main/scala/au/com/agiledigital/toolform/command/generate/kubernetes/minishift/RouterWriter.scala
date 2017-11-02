package au.com.agiledigital.toolform.command.generate.kubernetes.minishift

import au.com.agiledigital.toolform.command.generate.kubernetes.KubernetesWriter
import au.com.agiledigital.toolform.model.{Endpoint, TlsConfig}

object RouterWriter extends KubernetesWriter {

  private def writeToBlock(endpoint: Endpoint) =
    for {
      _ <- write("to:")
      _ <- indented {
            for {
              _ <- write("kind: Service")
              _ <- write(s"name: ${endpoint.target}")
            } yield ()
          }
    } yield ()

  private def writeTlsConfig(tlsConfig: TlsConfig): Result[Unit] =
    if (tlsConfig.enabled) {
      val tlsTerminationType            = tlsConfig.tlsTerminationType.toString
      val insecureEdgeTerminationPolicy = tlsConfig.tlsInsecureEdgePolicy.toString.toLowerCase
      for {
        _ <- write("tls:")
        _ <- indented {
              for {
                _ <- write(s"termination: $tlsTerminationType")
                _ <- write(s"insecureEdgeTerminationPolicy: $insecureEdgeTerminationPolicy")
              } yield ()
            }
      } yield ()
    } else {
      identity
    }

  /**
    * Writes a OpenShift "route" specification based on the provided toolform endpoint.
    *
    * A route specifies how the outside world communicates with internal services.
    *
    * @see https://docs.openshift.com/enterprise/3.0/architecture/core_concepts/routes.html
    *
    * @param endpointId the ID of the endpoint object which is used to generate certain names.
    * @param endpoint   the endpoint object that will be used to create the route spec.
    * @return           a state monad encapsulating the context of the writing process after the method has completed.
    */
  def writeRouter(endpointId: String, endpoint: Endpoint): Result[Unit] =
    for {
      _ <- write("---")
      _ <- write("apiVersion: v1")
      _ <- write("kind: Route")
      _ <- write("metadata:")
      _ <- indented {
            for {
              _ <- write(s"name: $endpointId")
            } yield ()
          }
      _ <- write("spec:")
      _ <- indented {
            for {
              _ <- writeToBlock(endpoint)
              _ <- writeTlsConfig(endpoint.tlsConfig)
            } yield ()
          }
    } yield ()
}
