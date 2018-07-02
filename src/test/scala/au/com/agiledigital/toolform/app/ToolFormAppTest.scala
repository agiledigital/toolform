package au.com.agiledigital.toolform.app

import java.io.File

import au.com.agiledigital.toolform.app.ToolFormAppSimulator.simulateAppForTest
import au.com.agiledigital.toolform.command.inspect.InspectCommand
import au.com.agiledigital.toolform.version.BuildInfo
import org.scalatest._
import com.monovore.decline._

class ToolFormAppTest extends FlatSpec with Matchers {

  private val testFile: File      = pathToFile("/testprojects/inspect/realworldsample/environment.conf")
  private val malformedFile: File = pathToFile("/errors/malformed.conf")

  private def pathToFile(pathToFile: String): File = {
    val url  = getClass.getResource(pathToFile)
    val file = new File(url.toURI)
    file
  }

  "inspect command" should "display an inspect summary for a valid file" in {
    val result = simulateAppForTest(List("inspect", "-i", testFile.getAbsolutePath).toArray)
    result should equal("""Project: [StruxureWare Insights Portal]
                         |	Components:
                         |		client/public ==> 'SE Public Web Application'
                         |		public-api ==> 'HTTP Public API'
                         |		se_swip_elastic-search ==> 'SE Elastic Search'
                         |		se-swip-influx-db ==> 'SE Influx DB'
                         |	Resources:
                         |		se-swip-carbon
                         |		se-swip-db
                         |		se-swip-mail-relay
                         |	Links:
                         |		se_swip_elastic-search -> public-api
                         |		se-swip-mail-relay -> public-api
                         |		se-swip-carbon -> public-api
                         |		se-swip-db -> public-api
                         |		se-swip-influx-db -> public-api
                         |""".stripMargin)
  }

  "inspect file that does not exist" should "display error string" in {
    val result = simulateAppForTest(List("inspect", "-i", "bad.txt").toArray)
    result should startWith("Input file [bad.txt] does not exist.")
  }

  "inspect malformed file" should "display error string" in {
    val result = simulateAppForTest(List("inspect", "-i", malformedFile.getAbsolutePath).toArray)
    result should startWith("Failed to parse project configuration")
  }

  "bad argument" should "display error string" in {
    val result = simulateAppForTest(List("--bad", "bad").toArray)
    result should startWith("Unexpected option: --bad")
  }

  "inspect with missing argument option" should "display error string" in {
    val result = simulateAppForTest(List("inspect", "-i").toArray)
    result should startWith("Missing value for option")
  }

  it should "fail if no arguments are specified" in {
    val result = simulateAppForTest(List("").toArray)
    result should startWith("Unexpected argument")
  }

  "plugin loader" should "load the inspect plugin" in {
    val plugins = ToolFormPluginLoader.plugins
    plugins.size should be > 1
    plugins.head shouldBe a[InspectCommand]
  }
}

object ToolFormAppSimulator {
  def simulateAppForTest(args: Array[String]): String = {
    val resultBuffer = new StringBuffer()
    val parserOpts = CliParserConfiguration.commandLineOptions.map {
      case Left((errors)) => resultBuffer.append(errors.toList.map({ _.message }).mkString(", ")); Unit
      case Right(result)  => resultBuffer.append(result); Unit
    }
    val showVersion = Opts
      .flag("version", "Print the version number and exit.", visibility = Visibility.Partial)
      .map(_ => resultBuffer.append(BuildInfo.version))

    val parser = Command(
      name = BuildInfo.name,
      header = "Generates deployment configuration from a project definition."
    )(showVersion orElse parserOpts)

    val parseResult: Either[Help, Any] = parser.parse(args)
    parseResult match {
      case Left(help) => resultBuffer.append(help)
      case Right(_)   => ()
    }
    resultBuffer.toString
  }
}
