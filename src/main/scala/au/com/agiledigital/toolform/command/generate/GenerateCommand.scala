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
        .map { (inputFile: Path, outputFile: Path, d: Unit) =>
          for {
            project <- ProjectReader.readProject(inputFile.toFile)
            status  <- runGenerateDockerComposeV3(inputFile.toFile.getAbsolutePath, outputFile.toFile, project)
          } yield status
        }
    }
}
