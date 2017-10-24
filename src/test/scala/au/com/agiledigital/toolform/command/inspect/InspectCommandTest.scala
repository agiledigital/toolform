package au.com.agiledigital.toolform.command.inspect

import java.io.File

import org.scalatest.EitherValues._
import org.scalatest.Inside.inside
import org.scalatest._

class InspectCommandTest extends FlatSpec with Matchers {

  val testFile: File  = pathToFile("/testprojects/inspect/realworldsample/environment.conf")
  val emptyFile: File = pathToFile("/errors/empty.conf")

  def pathToFile(pathToFile: String): File = {
    val url  = getClass.getResource(pathToFile)
    val file = new File(url.toURI)
    file
  }

  "inspect plugin" should "display an inspect summary for a valid file" in {
    val result = new InspectCommand().execute(testFile.toPath)
    inside(result) {
      case Right(s) =>
        s should equal("""Project: [StruxureWare Insights Portal]
                         |	Components:
                         |		public-api ==> 'HTTP Public API'
                         |		se_swip_elastic-search ==> 'SE Elastic Search'
                         |		se-swip-influx-db ==> 'SE Influx DB'
                         |		client/public ==> 'SE Public Web Application'
                         |	Resources:
                         |		se-swip-mail-relay
                         |		se-swip-carbon
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

  "error when reading project" should "display error string" in {
    val result = new InspectCommand().execute(emptyFile.toPath)
    result.left.value.message should startWith("Failed to read project")
  }
}
