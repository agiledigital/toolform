package au.com.agiledigital.toolform.command.generate.kubernetes

import java.io.{StringWriter}

import au.com.agiledigital.toolform.command.generate.kubernetes.VolumeClaimWriter._
import au.com.agiledigital.toolform.command.generate.WriterContext
import au.com.agiledigital.toolform.model._
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

// import scala.io.Source
// import collection.JavaConverters._
import com.typesafe.config.ConfigFactory

class VolumeClaimWriterTest extends FlatSpec with Matchers with PrivateMethodTester {
  "writeAccessModes" should "write access mode if a correct access mode is defined" in {
    val settings = """"settings": {
                      |  "accessModes": ["ReadWriteOnce"]
                      |  "paths": ["/var/lib/mount"]
                      |}""".stripMargin

    val settingsParam: Option[com.typesafe.config.Config] = Some(ConfigFactory.load(settings))
    val testResource: Resource                            = Resource("ID", "disk", settingsParam, storage = Option("2Gi"))
    val serviceName                                       = "testResource"

    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeAccessModes(testResource, serviceName).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("- ReadWriteOnce\n")
  }

  "writeAccessModes" should "write default ReadWriteOnce access mode if an incorrect access mode is defined" in {
    val settings = """"settings": {
                      |  "accessModes": ["IncorrectAccessMode"]
                      |  "paths": ["/var/lib/mount"]
                      |}""".stripMargin

    val settingsParam: Option[com.typesafe.config.Config] = Some(ConfigFactory.load(settings))
    val testResource: Resource                            = Resource("ID", "disk", settingsParam, storage = Option("2Gi"))
    val serviceName                                       = "testResource"

    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeAccessModes(testResource, serviceName).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("- ReadWriteOnce\n")
  }

  "writeAccessModes" should "only write correct access modes" in {
    val settings = """"settings": {
                      |  "accessModes": ["ReadOnlyMany", "IncorrectAccessMode", "alsoIncorrect"]
                      |  "paths": ["/var/lib/mount"]
                      |}""".stripMargin

    val settingsParam: Option[com.typesafe.config.Config] = Some(ConfigFactory.load(settings))
    val testResource: Resource                            = Resource("ID", "disk", settingsParam, storage = Option("2Gi"))
    val serviceName                                       = "testResource"

    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeAccessModes(testResource, serviceName).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("- ReadWriteOnce\n")
  }

  "writeAccessModes" should "not write access mode if no resource settings is defined" in {
    val testResource: Resource = Resource("ID", "disk", None, storage = Option("2Gi"))
    val serviceName            = "testResource"

    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeAccessModes(testResource, serviceName).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("- ReadWriteOnce\n")
  }

  "getStorageSize" should "write default 2Gi if no size defined" in {
    val testResource: Resource = Resource("ID", "disk", None, storage = None)
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("2Gi")
  }

  "getStorageSize" should "write storage size if size is defined" in {
    val testResource: Resource = Resource("ID", "disk", None, storage = Option("3Gi"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("3Gi")
  }

  "getStorageSize" should "write 2Gi storage size if size is 'small'" in {
    val testResource: Resource = Resource("ID", "disk", None, storage = Option("small"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("2Gi")
  }

  "getStorageSize" should "write 5Gi storage size if size is 'medium'" in {
    val testResource: Resource = Resource("ID", "disk", None, storage = Option("medium"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("5Gi")
  }

  "getStorageSize" should "write 10Gi storage size if size is 'large'" in {
    val testResource: Resource = Resource("ID", "disk", None, storage = Option("large"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("10Gi")
  }
}
