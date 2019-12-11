package au.com.agiledigital.toolform.command.generate.jenkins

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.{Files, Path, Paths}

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.plugin.ToolFormGenerateCommandPlugin
import cats.data.NonEmptyList
import cats.implicits._
import com.monovore.decline.Opts
import org.fusesource.scalate.TemplateEngine
import com.typesafe.config.{ ConfigFactory }

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
      (Opts.option[Path]("in-file", short = "i", metavar = "file", help = "the path to the project config file") |@|
        Opts.option[Path]("out-folder", short = "o", metavar = "folder", help = "the output folder for the config file") |@|
        Opts.option[String]("template", short = "t", metavar = "file", help = "the template for config map") |@|
        Opts.option[String]("namespace", short = "n", metavar = "namespace", help = "the namespace of kubernetes"))
        .map(execute)
    }

  def execute(inputFilePath: Path, outputFolderPath: Path, templateFilePath: String, namespace: String): Either[NonEmptyList[ToolFormError], String] = {
    val inputFile = inputFilePath.toFile
    val templateFile = Paths.get(templateFilePath).toFile
    val config = ConfigFactory.parseFile(inputFile)
    val name = config.getString("id")

    if (!inputFile.exists()) {
      Left(NonEmptyList.of(ToolFormError(s"Input file [$inputFile] does not exist.")))
    } else if (!inputFile.isFile) {
      Left(NonEmptyList.of(ToolFormError(s"Input file [$inputFile] is not a valid file.")))
    } else if (!templateFile.exists()) {
      Left(NonEmptyList.of(ToolFormError(s"Template file [$templateFile] does not exist.")))
    } else if (!inputFile.isFile) {
      Left(NonEmptyList.of(ToolFormError(s"Template file [$templateFile] is not a valid file.")))
    } else if (!Files.exists(outputFolderPath)) {
      Left(NonEmptyList.of(ToolFormError(s"Output directory [$outputFolderPath] does not exist.")))
    } else {
      for {
        status <- GenerateJenkinsConfigFilesCommand.runGenerateConfigFiles(name, namespace, name.concat("-job"), outputFolderPath, templateFilePath)
      } yield status
    }
  }
}

object GenerateJenkinsConfigFilesCommand {
  val engine = new TemplateEngine

  def runGenerateConfigFiles(name: String, namespace: String, job_name: String, outputFolderPath: Path, templatePath: String): Either[NonEmptyList[ToolFormError], String] =
    for {
      configFileStatus   <- writeConfigFile(name, namespace, job_name, outputFolderPath, templatePath)
    } yield s"$configFileStatus"

  def writeConfigFile(name: String, namespace: String, job_name: String, outputFolderPath: Path, templatePath: String): Either[NonEmptyList[ToolFormError], String] = {
    val configFile         = s"$outputFolderPath/config_map.yml"
    val templateMapping    = Map("name" -> name, "namespace" -> namespace, "job_name" -> job_name)

    val writer = new BufferedWriter(new FileWriter(configFile, false))

    val buildConfigFile = engine.layout(
      templatePath,
      templateMapping
    )

    try {
      writer.write(buildConfigFile)
      Right(s"Wrote the config map into $configFile")
    } finally {
      writer.close()
    }
  }
}
