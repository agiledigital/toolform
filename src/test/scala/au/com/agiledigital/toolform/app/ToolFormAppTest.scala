package au.com.agiledigital.toolform.app

import java.io.File

import org.scalatest._
import org.scalatest.Inside.inside

class ToolFormAppTest extends FlatSpec with Matchers {

  val testFile = pathToFile("/test_project/environment.conf")
  val emptyFile = pathToFile("/errors/empty.conf")
  val malformedFile = pathToFile("/errors/malformed.conf")

  def pathToFile(pathToFile: String): File = {
    val url = getClass.getResource(pathToFile)
    val file = new File(url.toURI())
    file
  }

  "inspect" should "display an inspect summary for a valid file" in {
    val result = ToolFormApp.execute(List("inspect", "-i", testFile.getAbsolutePath()).toArray)
    inside(result) {
      case Right(s) =>
        s should equal("""Project: [StruxureWare Insights Portal]
                                   |	Components:
                                   |		public-api ==> 'HTTP Public API'
                                   |		se_swip_elastic-search ==> 'SE Elastic Search'
                                   |		se-swip-influx-db ==> 'SE Influx DB'
                                   |		client/public ==> 'SE Public Web Application'
                                   |	Resources:
                                   |		se-swip-carbon
                                   |		se-swip-mail-relay
                                   |		se-swip-db
                                   |	Links:
                                   |		se_swip_elastic-search -> public-api
                                   |		se-swip-mail-relay -> public-api
                                   |		se-swip-carbon -> public-api
                                   |		se-swip-db -> public-api
                                   |		se-swip-influx-db -> public-api
                                   |""".stripMargin)
      case Left(error) => fail(error.message)
    }
  }

  "inspect blank file" should "display error string" in {
    val result = ToolFormApp.execute(List("inspect", "-i", emptyFile.getAbsolutePath()).toArray)
    result.left.get.message should startWith("Failed to read project")
  }

  "inspect file that does not exist" should "display error string" in {
    val result = ToolFormApp.execute(List("inspect", "-i", "bad.txt").toArray)
    result.left.get.message should equal("File [bad.txt] does not exist.")
  }

  "inspect malformed file" should "display error string" in {
    val result = ToolFormApp.execute(List("inspect", "-i", malformedFile.getAbsolutePath()).toArray)
    result.left.get.message should include("Failed to parse project configuration")
  }

  "bad argument" should "display error string" in {
    val result = ToolFormApp.execute(List("--bad", "bad").toArray)
    result.left.get.message should include("Invalid arguments")
  }

  "--inspect with missing argument option" should "display error string" in {
    val result = ToolFormApp.execute(List("inspect").toArray)
    result.left.get.message should include("Invalid arguments")
  }

  it should "fail if no arguments are specified" in {
    val result = ToolFormApp.execute(List("").toArray)
    result.left.get.message should include("Invalid arguments")
  }

  // TODO: Empty components/Resources/Links combos
  // TODO: Malformed components/Resources/Links combos
  // TODO: Test substitution
}
