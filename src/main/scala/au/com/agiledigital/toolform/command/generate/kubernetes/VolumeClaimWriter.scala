package au.com.agiledigital.toolform.command.generate.kubernetes

import au.com.agiledigital.toolform.model.{Resource}
import collection.JavaConversions._
import collection.JavaConverters._
import com.typesafe.config._
import cats.implicits._

object VolumeClaimWriter extends KubernetesWriter {

  // write the various access modes
  def writeAccessModes(resource: Resource): Result[Unit] = {
    val accessModes = resource.settings match {
      case Some(s) => {
        s.getStringList("accessModes")
      }
    }
    val modes = String.join("-", accessModes).split("-").toList

    for {
      _ <- modes.traverse_(mode => write(s"- $mode"))
    } yield ()
  }

  def writeVolumeClaim(projectId: String, resource: Resource): Result[Unit] = {
    val serviceName = determineServiceName(resource)
    val storage     = "TODO: parse storage!"

    for {
      _ <- write("---")
      _ <- write("apiVersion: extensions/v1beta1")
      _ <- write("kind: PersistentVolumeClaim")
      _ <- write("metadata:")
      _ <- indented {
            for {
              _ <- write(s"name: $serviceName")
            } yield ()
          }
      _ <- write("spec:")
      _ <- indented {
            for {
              _ <- write("resources:")
              _ <- indented {
                    for {
                      _ <- write("requests:")
                      _ <- indented {
                            for {
                              _ <- write(s"storage: $storage")
                            } yield ()
                          }
                    } yield ()
                  }
              _ <- write("accessModes:")
              _ <- writeAccessModes(resource)

            } yield ()
          }
    } yield ()
  }
}
