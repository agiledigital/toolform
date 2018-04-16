package au.com.agiledigital.toolform.command.generate.kubernetes

import au.com.agiledigital.toolform.model.{Resource}
import collection.JavaConversions._
import collection.JavaConverters._
import com.typesafe.config._
import cats.implicits._

object VolumeClaimWriter extends KubernetesWriter {

  // write the various access modes
  def writeAccessModes(resource: Resource, serviceName: String): Result[Unit] = {
    val acceptableModes = List("ReadWriteOnce", "ReadOnlyMany")

    val accessModes = resource.settings match {
      case Some(aModes) => {
        aModes.getStringList("accessModes")
      }
    }
    val modes = String.join("-", accessModes).split("-").toList
    for (mode <- modes) {
      if (!acceptableModes.contains(mode)) {
        println(s"Warning: $mode access mode unsupported for resource: $serviceName")
      }
    }

    for {
      _ <- modes.filter(mode => acceptableModes.contains(mode)).traverse_(mode => write(s"- $mode"))
    } yield ()
  }

  def writeVolumeClaim(projectId: String, resource: Resource): Result[Unit] = {
    val serviceName = determineServiceName(resource)
    // extract disk volume size information
    val storage = resource.storage match {
      case Some(strg) =>
        strg match {
          case "small"  => "2Gi"
          case "medium" => "5Gi"
          case "large"  => "10Gi"
          case _ => {
            if (strg.matches("([0-9]*Gi)")) {
              // trim leading zeros
              strg.replaceFirst("^0+(?!$)", "")
            } else {
              println(s"Warning: Invalid disk volume size '$strg' given for resource: $serviceName. Default size of 2Gi used.")
              "2Gi"
            }
          }
        }
      case None => { // use default and issue warning
        println(s"Warning: No disk volume size given for resource: $serviceName. Default size of 2Gi used.")
        "2Gi"
      }
    }

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
              _ <- writeAccessModes(resource, serviceName)

            } yield ()
          }
    } yield ()
  }
}
