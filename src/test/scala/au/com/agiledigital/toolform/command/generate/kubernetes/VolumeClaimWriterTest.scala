package au.com.agiledigital.toolform.command.generate.kubernetes

import java.io.{StringWriter}

import au.com.agiledigital.toolform.command.generate.kubernetes.VolumeClaimWriter._
import au.com.agiledigital.toolform.command.generate.WriterContext
import au.com.agiledigital.toolform.model._
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

class VolumeClaimWriterTest extends FlatSpec with Matchers with PrivateMethodTester {
  "writeAccessModes" should "write access mode if a correct access mode is defined" in {
    val accessModes                             = List("ReadOnlyMany")
    val paths                                   = List()
    val settingsParam: Option[ResourceSettings] = Some(ResourceSettings(accessModes, paths))

    val testResource: Resource = Resource("ID", "disk", settingsParam, None, storage = Option("2Gi"))
    val serviceName            = "testResource"

    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeAccessModes(testResource, serviceName).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("- ReadOnlyMany\n")
  }

  "writeAccessModes" should "write default access mode of ReadWriteOnce no access modes are defined" in {
    val accessModes                             = List()
    val paths                                   = List()
    val settingsParam: Option[ResourceSettings] = Some(ResourceSettings(accessModes, paths))

    val testResource: Resource = Resource("ID", "disk", settingsParam, None, storage = Option("2Gi"))
    val serviceName            = "testResource"

    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeAccessModes(testResource, serviceName).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("- ReadWriteOnce\n")
  }

  "writeAccessModes" should "throw an exception if an incorrect access mode is defined" in {
    val accessModes                             = List("IncorrectAccessMode")
    val paths                                   = List()
    val settingsParam: Option[ResourceSettings] = Some(ResourceSettings(accessModes, paths))

    val testResource: Resource = Resource("ID", "disk", settingsParam, None, storage = Option("2Gi"))
    val serviceName            = "testResource"

    val expectedMessage = "Unsupported access mode(s) [IncorrectAccessMode] for resource [ID]"
    the[IllegalArgumentException] thrownBy (writeAccessModes(testResource, serviceName)) should have message expectedMessage
  }

  "writeAccessModes" should "throw an exception if multiple incorrect access mode are defined" in {
    val accessModes                             = List("ReadOnlyMany", "IncorrectAccessMode", "alsoIncorrect")
    val paths                                   = List()
    val settingsParam: Option[ResourceSettings] = Some(ResourceSettings(accessModes, paths))

    val testResource: Resource = Resource("ID", "disk", settingsParam, None, storage = Option("2Gi"))
    val serviceName            = "testResource"

    val expectedMessage = "Unsupported access mode(s) [IncorrectAccessMode,alsoIncorrect] for resource [ID]"
    the[IllegalArgumentException] thrownBy (writeAccessModes(testResource, serviceName)) should have message expectedMessage
  }

  "writeAccessModes" should "throw an exception if no resource settings is defined" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = Option("2Gi"))
    val serviceName            = "testResource"

    val expectedMessage = "[ID] resource requires a settings object"
    the[IllegalArgumentException] thrownBy (writeAccessModes(testResource, serviceName)) should have message expectedMessage
  }

  "getStorageSize" should "write default 2Gi if no size defined" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = None)
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("2Gi")
  }

  "getStorageSize" should "write storage size if size is defined" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = Option("3Gi"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("3Gi")
  }

  "getStorageSize" should "write 2Gi storage size if size is 'small'" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = Option("small"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("2Gi")
  }

  "getStorageSize" should "write 5Gi storage size if size is 'medium'" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = Option("medium"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("5Gi")
  }

  "getStorageSize" should "write 10Gi storage size if size is 'large'" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = Option("large"))
    val serviceName            = "testResource"

    getStorageSize(testResource, serviceName) should equal("10Gi")
  }
}
