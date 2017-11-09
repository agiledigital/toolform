package au.com.agiledigital.toolform.app

import java.util.ServiceLoader

import au.com.agiledigital.toolform.plugin.ToolFormCommandPlugin
import au.com.agiledigital.toolform.version.BuildInfo
import cats.data.NonEmptyList
import com.monovore.decline._

import scala.collection.immutable.Seq
import scala.collection.JavaConverters._

/**
  * A tool to generate CI/CD pipelines from a project definition.
  */
object ToolFormApp
    extends CommandApp(
      name = BuildInfo.name,
      version = BuildInfo.version,
      header = "Generates deployment configuration from a project definition.",
      main = CliParserConfiguration.commandLineOptions.map {
        case Left(errors) =>
          System.err.println("Failed due to the following errors:")
          errors.toList.map({ _.message }).foreach(System.err.println)
          System.exit(1)
        case Right(output) =>
          println(output)
          System.exit(0)
      }
    )

/**
  * Builds command line parser options that include all available
  * toolform commands.  The options are in monovore/decline style, accessed
  * via the [[Opts]] trait and runnable by wrapping in a [[Command]] or
  * [[CommandApp]].
  */
object CliParserConfiguration {

  /**
    * Command line parser options.
    * @return Opts style command line parser options for use in a decline
    *         parser/app runner.
    */
  def commandLineOptions: Opts[Either[NonEmptyList[ToolFormError], String]] =
    ToolFormPluginLoader.plugins.map(_.command).reduce(_ orElse _)
}

/**
  * Discovers and loads all available [[ToolFormCommandPlugin]]s.
  */
object ToolFormPluginLoader {

  /**
    * Available command plugins loaded from the runtime environment.
    * @return Collection of command plugins.
    */
  def plugins: Seq[ToolFormCommandPlugin] = {
    val serviceLoaderIterator                          = ServiceLoader.load(classOf[ToolFormCommandPlugin]).iterator()
    val scalaIterator: Iterator[ToolFormCommandPlugin] = serviceLoaderIterator.asScala
    scalaIterator.toIndexedSeq
  }
}

/**
  * A simple error type for the toolform CLI app.
  *
  * @param message The error detail message.
  */
final case class ToolFormError(message: String)
