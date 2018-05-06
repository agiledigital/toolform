package au.com.agiledigital.toolform.command.generate.kubernetes

import au.com.agiledigital.toolform.model.{Resource}
import collection.JavaConversions._
import collection.JavaConverters._
import com.typesafe.config._
import cats.implicits._
import scala.util.{Failure, Success, Try}

object VolumeClaimWriter extends KubernetesWriter {

  // write the various access modes
  def writeAccessModes(resource: Resource, serviceName: String): Result[Unit] = {
    val acceptableModes = List("ReadWriteOnce", "ReadOnlyMany")
    val accessModes = resource.settings match {
      case Some(aModes) => {
        Try(aModes.getStringList("accessModes")) match {
          case Success(list) => list
          // no accessModes specified in project.conf
          case Failure(error) => {
            println(s"Warning: no access modes specified for resource: $serviceName. Default of ReadWriteOnce used")
            val res: java.util.List[String] = List("ReadWriteOnce")
            res
          }
        }
      }
      case None => {
        println(s"Warning: no access modes specified for resource: $serviceName. Default of ReadWriteOnce used")
        val res: java.util.List[String] = List("ReadWriteOnce")
        res
      }
    }
    val modes = String.join("-", accessModes).split("-").toList
    for (mode <- modes) {
      if (!acceptableModes.contains(mode)) {
        println(s"Warning: $mode access mode unsupported for resource: $serviceName")
      }
    }

    for {
      _ <- modes.traverse_(mode => write(s"- $mode"))
    } yield ()
  }

  // return the storage size of the disk resource, printing a warning and using default if no size specified
  def getStorageSize(resource: Resource, serviceName: String): String =
    resource.storage match {
      case Some(strg) =>
        strg match {
          case "small"  => "2Gi"
          case "medium" => "5Gi"
          case "large"  => "10Gi"
          case _        => strg
        }
      case None => { // use default and issue warning
        println(s"Warning: No disk volume size given for resource: $serviceName. Default size of 2Gi used.")
        "2Gi"
      }
    }

  /**
    * Writes a Kubernetes "PersistentVolumeClaim" specification based on the provided disk resource.
    *
    * A PersistentVolumeClaim is a request for storage by a user
    *
    * @see https://kubernetes.io/docs/concepts/storage/persistent-volumes/
    *
    * @param resource   the disk resource object that will be used to create the PersistentVolumeClaim.
    * @return           a state monad encapsulating the context of the writing process after the method has completed.
    */
  def writeVolumeClaim(resource: Resource): Result[Unit] = {
    val serviceName = determineServiceName(resource)
    val storage     = getStorageSize(resource, serviceName)

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
