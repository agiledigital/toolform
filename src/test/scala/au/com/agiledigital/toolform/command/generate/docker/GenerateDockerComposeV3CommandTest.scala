package au.com.agiledigital.toolform.command.generate.docker

import java.io.{File, StringWriter}

import au.com.agiledigital.toolform.app.ToolFormAppSimulator
import au.com.agiledigital.toolform.command.generate.WriterContext
import au.com.agiledigital.toolform.command.generate.docker.GenerateDockerComposeV3Command._
import au.com.agiledigital.toolform.model._
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

import scala.compat.Platform.EOL
import scala.io.Source

class GenerateDockerComposeV3CommandTest extends FlatSpec with Matchers with PrivateMethodTester {

  private val rootTestFolder: File = pathToFile("/testprojects/dockercomposev3")

  private def pathToFile(pathToFile: String): File = {
    val url  = getClass.getResource(pathToFile)
    val file = new File(url.toURI)
    file
  }

  /**
    * Strips out all the lines starting with a hash and returns them as joined string.
    * This is used because the commented lines change with the current date/app version etc. and are not stable.
    * @param file the file to read from.
    * @return a string representing the specified file with the commented lines removed.
    */
  private def readFileIgnoringComments(file: File) =
    Source
      .fromFile(file.getAbsolutePath)
      .getLines()
      .filterNot(line => line.startsWith("#"))
      .mkString(EOL)

  private val testFolders = rootTestFolder
    .listFiles()
    .filter(_.isDirectory)

  for (folder <- testFolders) {
    "runDockerComposeV3" should s"generate valid Docker Compose v3 files for scenario: ${folder.getName}" in {
      val inputFile    = new File(s"${folder.getAbsolutePath}/environment.conf")
      val expectedFile = new File(s"${folder.getAbsolutePath}/expected.yaml")
      val outputFile   = File.createTempFile(getClass.getName, ".yaml")
      outputFile.deleteOnExit()
      val result = new GenerateDockerComposeV3Command().execute(inputFile.toPath, outputFile.toPath)
      result match {
        case Right(_) =>
          val actual   = readFileIgnoringComments(outputFile)
          val expected = readFileIgnoringComments(expectedFile)
          actual should equal(expected)
        case Left(errors) => fail(errors.toList.mkString(", "))
      }
    }
  }

  "generate docker output with invalid out dir" should "fail with error" in {
    val inputFile: File = pathToFile("/testprojects/dockercomposev3/realworldsample/environment.conf")
    val result          = ToolFormAppSimulator.simulateAppForTest(List("generate", "dockercompose", "-i", inputFile.getAbsolutePath, "-o", "/tmp/foo/bar/baz/generate-docker-test.out").toArray)
    result should startWith("Output directory [/tmp/foo/bar/baz] does not exist")
  }

  "writePorts" should "write ports if exposedPorts is defined" in {
    val testService = new ToolFormService {
      def id          = ""
      def environment = Map()
      def externalPorts = List(
        PortMapping(80, 80),
        PortMapping(90, 90, PortProtocolType.Udp),
        PortMapping(443, 8080),
        PortMapping(9999999, 9999999, PortProtocolType.Udp)
      )
      def exposedPorts = List()
    }
    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writePorts(testService).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal(
      s"""ports:
         |- "80/tcp"
         |- "90/udp"
         |- "443:8080/tcp"
         |- "9999999/udp"
         |""".stripMargin
    )
  }

  "writePorts" should "not write ports if exposedPorts is defined" in {
    // Unlike Kubernetes, Docker Compose can detect exposed ports automatically using the image definition.
    // They don't need to be written to the final config file.
    val testService = new ToolFormService {
      def id            = ""
      def environment   = Map()
      def externalPorts = List()
      def exposedPorts = List(
        PortMapping(80, 80),
        PortMapping(443, 443),
        PortMapping(33, 44, PortProtocolType.Udp)
      )
    }
    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writePorts(testService).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal("")
  }

  "writePorts" should "not write anything if empty list of ports defined" in {
    val testService = new ToolFormService {
      def id            = ""
      def environment   = Map()
      def externalPorts = List()
      def exposedPorts  = List()
    }
    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writePorts(testService).run(testContext).value

    testWriter.toString should equal("")
  }

  "writeEnvironmentVariables" should "write environment variables if environment is defined" in {
    val testService = new ToolFormService {
      def id = ""
      def environment =
        Map(
          "ABC" -> "DEF",
          "123" -> "345"
        )
      def externalPorts = List()
      def exposedPorts  = List()
    }
    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeEnvironmentVariables(testService).run(testContext).value

    print(testWriter.toString)

    testWriter.toString should equal(
      s"""environment:
         |- ABC=DEF
         |- 123=345
         |""".stripMargin
    )
  }

  "writeEnvironmentVariables" should "not write anything if empty map of environment variables defined" in {
    val testService = new ToolFormService {
      def id            = ""
      def environment   = Map()
      def externalPorts = List()
      def exposedPorts  = List()
    }
    val testWriter  = new StringWriter()
    val testContext = WriterContext(testWriter)

    writeEnvironmentVariables(testService).run(testContext).value

    testWriter.toString should equal("")
  }

  "writeResources" should "not write anything if an empty list of resources is provided" in {
    val testResources = List[Resource]()
    val testWriter    = new StringWriter()
    val testContext   = WriterContext(testWriter)

    writeResources(testResources).run(testContext).value

    testWriter.toString should equal("")
  }

  "writeComponents" should "not write anything if an empty list of components is provided" in {
    val testComponents = List[Component]()
    val testWriter     = new StringWriter()
    val testContext    = WriterContext(testWriter)

    writeComponents("", testComponents).run(testContext).value

    testWriter.toString should equal("")
  }
}
