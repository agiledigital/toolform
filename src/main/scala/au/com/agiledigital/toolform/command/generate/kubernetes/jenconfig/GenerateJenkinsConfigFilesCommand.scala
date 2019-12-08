package au.com.agiledigital.toolform.command.generate.jenkins

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Path, Paths}

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.model.Project
import au.com.agiledigital.toolform.plugin.ToolFormGenerateCommandPlugin
import au.com.agiledigital.toolform.reader.ProjectReader
import cats.data.NonEmptyList
import cats.implicits._
import com.monovore.decline.Opts
import org.fusesource.scalate.TemplateEngine

/**
  * Takes an abstract project definition and outputs it to a file in the Jenkinsfile format.
  *
  * It outputs build.Jenkinsfile and deploy.Jenkinsfile.
  *
  * @see https://jenkins.io/doc/book/pipeline/jenkinsfile/
  */
class GenerateJenkinsConfigFilesCommand extends ToolFormGenerateCommandPlugin {

  /**
    * The primary class for generating Jenkins files.
    */
  def command: Opts[Either[NonEmptyList[ToolFormError], String]] =
    Opts.subcommand("jenkinsconfigfiles", "Generate config files and deploy them on jenkins") {
      (Opts.option[String]("name", short = "n", metavar = "name", help = "the name of the config map") |@|
        Opts.option[String]("namespace", short = "ns", metavar = "namespace", help = "the namespace in kubernetes") |@|
        Opts.option[String]("instance", short = "i", metavar = "instance", help = "the instance in kubernetes") |@|
        Opts.option[String]("volume", short = "v", metavar = "volume", help = "the name of the volume it will mount on") |@|
        Opts.option[String]("repo", short = "r", metavar = "repo", help = "the name of the repo that contains the code"))
        .map(execute)
    }

  def execute(name: String, namespace: String, instance: String, volume: String, repo: String): Either[NonEmptyList[ToolFormError], String] =
    for {
      status <- GenerateJenkinsConfigFilesCommand.runGenerateConfigFiles(name, namespace, instance, volume, repo)
    } yield status
}

object GenerateJenkinsConfigFilesCommand {
  val engine = new TemplateEngine

  def runGenerateConfigFiles(name: String, namespace: String, instance: String, volume: String, repo: String): Either[NonEmptyList[ToolFormError], String] =
    for {
      configFileStatus   <- writeConfigFile(name, namespace, instance)
      dispatchFileStatus <- writeDispatchFile(name, namespace, repo, volume)
    } yield s"$configFileStatus\n$dispatchFileStatus"

  def writeConfigFile(name: String, namespace: String, instance: String): Either[NonEmptyList[ToolFormError], String] = {
    val configFile         = "configMaps/config_map.yml"
    val configFileTemplate = "configMaps/config_map.yml.mustache"
    val templateMapping    = Map("name" -> s"$name", "namespace" -> s"$namespace", "instance" -> s"$instance")

    val writer = new BufferedWriter(new FileWriter(configFile, false))

    val buildConfigFile = engine.layout(
      configFileTemplate,
      templateMapping
    )

    try {
      writer.write(buildConfigFile)
      Right(s"Wrote the config map into $configFile")
    } finally {
      writer.close()
    }
  }

  def writeDispatchFile(name: String, namespace: String, repo: String, volume: String): Either[NonEmptyList[ToolFormError], String] = {
    val dispatchFile         = "configMaps/dep_patch.yml"
    val dispatchFileTemplate = "configMaps/dep_patch.yml.mustache"
    val templateMapping      = Map("name" -> s"$name", "namespace" -> s"$namespace", "repo" -> s"$repo", "volume" -> s"$volume")

    val writer = new BufferedWriter(new FileWriter(dispatchFile, false))
    val buildDispatchFile = engine.layout(
      dispatchFileTemplate,
      templateMapping
    )

    try {
      writer.write(buildDispatchFile)
      Right(s"Wrote the dispatch file into $dispatchFile")
    } finally {
      writer.close()
    }
  }
}
