package au.com.agiledigital.toolform.command.generate

import java.util.ServiceLoader

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.plugin.{ToolFormCommandPlugin, ToolFormGenerateCommandPlugin}
import cats.data.NonEmptyList
import com.monovore.decline._

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

/**
  * Exposes [[ToolFormGenerateCommandPlugin]]s under the generate sub command.
  */
class GenerateCommand extends ToolFormCommandPlugin {

  override val command: Opts[Either[NonEmptyList[ToolFormError], String]] = {
    Opts
      .subcommand("generate", "Generate config files targeting a particular platform") {
        GenerateCommand.plugins.map(_.command).reduce(_ orElse _)
      }
  }
}

/**
  * Discovers and loads all available [[ToolFormGenerateCommandPlugin]]s.
  */
object GenerateCommand {

  /**
    * Available command plugins loaded from the runtime environment.
    *
    * @return Collection of command plugins.
    */
  def plugins: Seq[ToolFormGenerateCommandPlugin] = {
    val serviceLoaderIterator                                  = ServiceLoader.load(classOf[ToolFormGenerateCommandPlugin]).iterator()
    val scalaIterator: Iterator[ToolFormGenerateCommandPlugin] = serviceLoaderIterator.asScala
    scalaIterator.toIndexedSeq
  }
}
