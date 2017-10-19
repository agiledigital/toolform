package au.com.agiledigital.toolform.app

import java.util.ServiceLoader

import au.com.agiledigital.toolform.plugin.ToolFormPlugin
import au.com.agiledigital.toolform.version.BuildInfo
import com.monovore.decline._

import scala.collection.JavaConverters._

/**
  * A tool to generate CI/CD pipelines from a project definition.
  */
object ToolFormApp
    extends CommandApp(
      name = BuildInfo.name,
      version = BuildInfo.version,
      header = "Generates deployment configuration from a project definition.",
      main = CliParserOptions.commandLineOptions.map {
        case Left(error) =>
          Console.err.println(error)
          System.exit(1)
        case Right(output) =>
          println(output)
          System.exit(0)
      }
    )

object CliParserOptions {
  val commandLineOptions: Opts[Either[ToolFormError, String]] =
    ToolFormPluginLoader.loadPlugins.map(_.command).reduce(_ orElse _)
}

object ToolFormPluginLoader {
  def loadPlugins: Seq[ToolFormPlugin] =
    ServiceLoader.load(classOf[ToolFormPlugin]).asScala.toSeq
}

/**
  * A simple error type for the toolform CLI app.
  *
  * @param message The error detail message.
  */
final case class ToolFormError(message: String)
