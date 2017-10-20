package au.com.agiledigital.toolform.command.generate.minikube

import java.io.File

import au.com.agiledigital.toolform.app.ToolFormApp
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}
import scala.compat.Platform.EOL

import scala.io.Source

class GenerateMinikubeTest extends FlatSpec with Matchers with PrivateMethodTester {

  private val rootTestFolder: File = pathToFile("/testprojects/minikube")

  private def pathToFile(pathToFile: String): File = {
    val url = getClass.getResource(pathToFile)
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
      val result = ToolFormApp.execute(List("generate", "-k", "-i", inputFile.getAbsolutePath, "-o", outputFile.getAbsolutePath).toArray)
      result match {
        case Right(_) =>
          val actual   = readFileIgnoringComments(outputFile)
          val expected = readFileIgnoringComments(expectedFile)
          actual should equal(expected)
        case Left(error) => fail(error.message)
      }
    }
  }
}
