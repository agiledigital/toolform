package au.com.agiledigital.toolform.app

import com.typesafe.config._

/**
  * Toolform is a CLI app that generates CI/CD pipelines from a project definition.
  */
object ToolFormApp extends App {

  /**
    * Runs the tool.
    *
    * @param args Command line arguments.
    * @return Either[String, Config] - The configuration or an error.
    */
  def execute(args: Array[String]): Either[String, Config] = {
    val parser = new scopt.OptionParser[CommandConfig]("toolform") {
      head("toolform", "0.1")
      help("help").abbr("h").text("Displays this usage text.")
      version("version").abbr("v").text("Displays version information.")
    }

    parser.parse(args, CommandConfig()) match {
      case Some(commandConfig) => Right(ConfigFactory.empty())
      case None                => Left(s"Invalid arguments - toolform failed.")
    }
  }

  def displayConfiguration(config: Config): Unit = {
    val renderOptions: ConfigRenderOptions =
      ConfigRenderOptions
        .defaults()
        .setComments(false)
        .setOriginComments(false)
    println(config.root().render(renderOptions))
  }

  execute(args) match {
    case Left(error) =>
      Console.err.println(error)
      sys.exit(1)
    case Right(config) =>
      displayConfiguration(config)
  }
}

/**
  * The configuration for the tool
  */
final case class CommandConfig() {}
