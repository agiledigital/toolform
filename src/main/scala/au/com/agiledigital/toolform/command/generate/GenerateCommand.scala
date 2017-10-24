package au.com.agiledigital.toolform.command.generate

import java.nio.file.Path

import au.com.agiledigital.toolform.app.ToolFormError
import au.com.agiledigital.toolform.plugin.ToolFormCommandPlugin
import au.com.agiledigital.toolform.reader.ProjectReader
import au.com.agiledigital.toolform.command.generate.docker.GenerateDockerComposeV3.runGenerateDockerComposeV3
import com.monovore.decline._
import cats.implicits._

/**
  * The primary class for generating config files.
  * You should use this class as it will automatically delegate to the relevant subtask according to the configuration
  * parsed on the command line.
  */
class GenerateCommand() extends ToolFormCommandPlugin {

  def command: Opts[Either[ToolFormError, String]] =
    Opts.subcommand("generate", "generates config files for container orchestration.") {
      (Opts.option[Path]("in-file", short = "i", metavar = "file", help = "the path to the project config file") |@|
        Opts.option[Path]("out-file", short = "o", metavar = "file", help = "the path to output the generated file(s)") |@|
        Opts.flag("generate-docker-compose", short = "d", help = "generate a Docker Compose v3 file as output (default)"))
        .map { (inputFilePath: Path, outputFilePath: Path, d: Unit) =>
          val inputFile = inputFilePath.toFile
          val outputFile = outputFilePath.toFile
          if (!inputFile.exists()) {
            Left(ToolFormError(s"Input file [${inputFile}] does not exist."))
          } else if (!inputFile.isFile) {
            Left(ToolFormError(s"Input file [${inputFile}] is not a valid file."))
          } else if (!outputFile.getParentFile.exists()) {
            Left(ToolFormError(s"Output directory [${outputFile.getParentFile}] does not exist."))
          } else {
            for {
              project <- ProjectReader.readProject(inputFile)
              status  <- runGenerateDockerComposeV3(inputFile.getAbsolutePath, outputFile, project)
            } yield status
          }
        }
    }
}
