package au.com.agiledigital.toolform.command.generate.jenkins

import java.io.File
import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}

import scala.io.Source

class GenerateJenkinsFilesCommandTest extends FlatSpec with Matchers with PrivateMethodTester {

  private val rootTestFolder: File = pathToFile("/testprojects/jenkins")

  private def pathToFile(pathToFile: String): File = {
    val url  = getClass.getResource(pathToFile)
    val file = new File(url.toURI)
    file
  }

  private def readFile(file: File) =
    Source
      .fromFile(file.getAbsolutePath)
      .getLines()
      .map(line => line.trim)
      .mkString("")
      .replaceAll("\\s", "")

  private val testFolders = rootTestFolder
    .listFiles()
    .filter(_.isDirectory)

  for (folder <- testFolders) {
    "runJenkinsfiles" should s"generate valid Jenkins files for scenario: ${folder.getName}" in {
      val inputFile                 = new File(s"${folder.getAbsolutePath}/environment.conf")
      val expectedBuildJenkinsFile  = new File(s"${folder.getAbsolutePath}/expected.build.Jenkinsfile")
      val expectedDeployJenkinsFile = new File(s"${folder.getAbsolutePath}/expected.deploy.Jenkinsfile")
      val outputFolder              = File.createTempFile(getClass.getName, "")
      outputFolder.deleteOnExit()
      outputFolder.delete()
      outputFolder.mkdir()
      val result = new GenerateJenkinsFilesCommand().execute(inputFile.toPath, outputFolder.toPath, s"${folder.getAbsolutePath}")
      result match {
        case Right(_) =>
          val actualBuildJenkinsFile  = readFile(Paths.get(s"$outputFolder/build.Jenkinsfile").toFile)
          val actualDeployJenkinsFile = readFile(Paths.get(s"$outputFolder/deploy.Jenkinsfile").toFile)
          val expectedBuildFile       = readFile(expectedBuildJenkinsFile)
          val expectedDeployFile      = readFile(expectedDeployJenkinsFile)

          actualBuildJenkinsFile should equal(expectedBuildFile)
          actualDeployJenkinsFile should equal(expectedDeployFile)
        case Left(errors) => fail(errors.toList.mkString(", "))
      }
    }
  }
}
