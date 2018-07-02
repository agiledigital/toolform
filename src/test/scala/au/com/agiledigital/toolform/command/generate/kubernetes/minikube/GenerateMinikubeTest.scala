package au.com.agiledigital.toolform.command.generate.kubernetes.minikube

import java.io.File

import au.com.agiledigital.toolform.app.ToolFormAppSimulator
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}
import au.com.agiledigital.toolform.model._

import scala.compat.Platform.EOL
import scala.io.Source

class GenerateMinikubeTest extends FlatSpec with Matchers with PrivateMethodTester {

  private val rootTestFolder: File = pathToFile("/testprojects/minikube")

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
    "runGenerateMinikube" should s"generate valid Kubernetes (Minikube flavour) specs for scenario: ${folder.getName}" in {
      val inputFile    = new File(s"${folder.getAbsolutePath}/environment.conf")
      val expectedFile = new File(s"${folder.getAbsolutePath}/expected.yaml")
      val outputFile   = File.createTempFile(getClass.getName, ".yaml")
      outputFile.deleteOnExit()
      val result = new GenerateMinikubeCommand().execute(inputFile.toPath, outputFile.toPath)
      result match {
        case Right(_) =>
          val actual   = readFileIgnoringComments(outputFile)
          val expected = readFileIgnoringComments(expectedFile)
          actual should equal(expected)
        case Left(errors) => fail(errors.toList.mkString(", "))
      }
    }
  }

  "generate minikube output with invalid out dir" should "fail with error" in {
    val inputFile: File = pathToFile("/testprojects/minikube/realworldsample/environment.conf")
    val result          = ToolFormAppSimulator.simulateAppForTest(List("generate", "minikube", "-i", inputFile.getAbsolutePath, "-o", "/tmp/foo/bar/baz/generate-docker-test.out").toArray)
    result should startWith("Output directory [/tmp/foo/bar/baz] does not exist")
  }

  "isDiskResourceType" should "should return true if resource is disk type" in {
    val testResource: Resource = Resource("ID", "disk", None, None, storage = None)

    val isDiskResourceType = PrivateMethod[GenerateMinikubeCommand]('isDiskResourceType)
    GenerateMinikubeCommand invokePrivate isDiskResourceType(testResource) should equal(true)
  }

  "isDiskResourceType" should "should return false if resource is not disk type" in {
    val testResource: Resource = Resource("ID", "notDisk", None, None, storage = None)

    val isDiskResourceType = PrivateMethod[GenerateMinikubeCommand]('isDiskResourceType)
    GenerateMinikubeCommand invokePrivate isDiskResourceType(testResource) should equal(false)
  }
}
