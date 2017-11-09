package au.com.agiledigital.toolform.reader

import java.io.File

import au.com.agiledigital.toolform.model.Project
import org.scalatest.EitherValues._
import org.scalatest.Inside.inside
import org.scalatest._

class ProjectReaderTest extends FlatSpec with Matchers {

  val testFile      = pathToFile("/testprojects/inspect/realworldsample/environment.conf")
  val emptyFile     = pathToFile("/errors/empty.conf")
  val malformedFile = pathToFile("/errors/malformed.conf")

  def pathToFile(pathToFile: String): File = {
    val url  = getClass.getResource(pathToFile)
    val file = new File(url.toURI)
    file
  }

  "reading a valid project" should "return a Project model" in {
    val result = ProjectReader.readProject(testFile)
    inside(result) {
      case Right(project) => project shouldBe a[Project]
      case Left(errors)   => fail(errors.toList.mkString(", "))
    }
  }

  "inspect blank file" should "display error string" in {
    val result = ProjectReader.readProject(emptyFile)
    val errors = result.left.value.toList
    errors should have length 1
    errors.head.message should startWith("Failed to read project")
  }

  "reading file that does not exist" should "display error string" in {
    val result = ProjectReader.readProject(new File("bad.txt"))
    val errors = result.left.value.toList
    errors should have length 1
    errors.head.message should startWith("File [bad.txt] does not exist.")
  }

  "inspect malformed file" should "display error string" in {
    val result = ProjectReader.readProject(malformedFile)
    val errors = result.left.value.toList
    errors should have length 1
    errors.head.message should startWith("Failed to parse project configuration")
  }

  // TODO: Empty components/Resources/Links combos
  // TODO: Malformed components/Resources/Links combos
  // TODO: Test substitution
}
