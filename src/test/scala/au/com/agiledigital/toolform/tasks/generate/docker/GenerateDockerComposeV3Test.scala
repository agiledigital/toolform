package au.com.agiledigital.toolform.tasks.generate.docker

import java.io.StringWriter

import au.com.agiledigital.toolform.model.{Component, Resource, Service}
import au.com.agiledigital.toolform.tasks.generate.WriterContext
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

class GenerateDockerComposeV3Test extends FlatSpec with Matchers with PrivateMethodTester {

  "writePorts" should "write ports if exposedPorts is defined" in {
    val testService = new Service {
      def environment = None
      def exposedPorts = Some(List("80:80", "443:443", "anything", "9999999"))
    }
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writePorts(testService).exec(testContext)

    print(testWriter.toString)

    testWriter.toString should equal(
      s"""ports:
         |- "80:80"
         |- "443:443"
         |- "anything"
         |- "9999999"
         |""".stripMargin
    )
  }

  "writePorts" should "not write anything if no ports defined" in {
    val testService = new Service {
      def environment = None
      def exposedPorts = None
    }
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writePorts(testService).exec(testContext)

    testWriter.toString should equal("")
  }

  "writePorts" should "not write anything if empty list of ports defined" in {
    val testService = new Service {
      def environment = None
      def exposedPorts = Some(List())
    }
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writePorts(testService).exec(testContext)

    testWriter.toString should equal("")
  }

  "writeEnvironmentVariables" should "write environment variables if environment is defined" in {
    val testService = new Service {
      def environment =
        Some(
          Map(
            "ABC" -> "DEF",
            "123" -> "345"
          ))
      def exposedPorts = None
    }
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writeEnvironmentVariables(testService).exec(testContext)

    print(testWriter.toString)

    testWriter.toString should equal(
      s"""environment:
         |- ABC=DEF
         |- 123=345
         |""".stripMargin
    )
  }

  "writeEnvironmentVariables" should "not write anything if no environment defined" in {
    val testService = new Service {
      def environment = None
      def exposedPorts = None
    }
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writeEnvironmentVariables(testService).exec(testContext)

    testWriter.toString should equal("")
  }

  "writeEnvironmentVariables" should "not write anything if empty map of environment variables defined" in {
    val testService = new Service {
      def environment = Some(Map())
      def exposedPorts = None
    }
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writeEnvironmentVariables(testService).exec(testContext)

    testWriter.toString should equal("")
  }

  "writeEdges" should "not write anything if an empty list of edges is provided" in {
    val testEdges = List[SubEdgeDef]()
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writeEdges("", testEdges).exec(testContext)

    testWriter.toString should equal("")
  }

  "writeResources" should "not write anything if an empty list of resources is provided" in {
    val testResources = List[Resource]()
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writeResources(testResources).exec(testContext)

    testWriter.toString should equal("")
  }

  "writeComponents" should "not write anything if an empty list of components is provided" in {
    val testComponents = List[Component]()
    val testTarget = new GenerateDockerComposeV3()
    val testWriter = new StringWriter()
    val testContext = WriterContext(testWriter)

    testTarget.writeComponents("", testComponents).exec(testContext)

    testWriter.toString should equal("")
  }
}
