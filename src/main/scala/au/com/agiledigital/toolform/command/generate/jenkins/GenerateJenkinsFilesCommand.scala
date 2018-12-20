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
class GenerateJenkinsFilesCommand extends ToolFormGenerateCommandPlugin {

  /**
    * The primary class for generating Jenkins files.
    */
  def command: Opts[Either[NonEmptyList[ToolFormError], String]] =
    Opts.subcommand("jenkinsfiles", "generates Jenkins files for Jenkins to build/deploy the project") {
      (Opts.option[Path]("in-file", short = "i", metavar = "file", help = "the path to the project config file") |@|
        Opts.option[Path]("out-file", short = "o", metavar = "file", help = "the path to output the generated file(s)") |@|
        Opts.option[String]("template-folder-path", short = "t", metavar = "path", help = "the path to the input template file(s)"))
        .map(execute)
    }

  def execute(inputFilePath: Path, outputFolderPath: Path, templateFolderPath: String): Either[NonEmptyList[ToolFormError], String] = {
    val inputFile    = inputFilePath.toFile
    val outputFolder = outputFolderPath.toAbsolutePath.toString
    val buildFile    = Paths.get(s"$outputFolder/build.Jenkinsfile").toFile
    val deployFile   = Paths.get(s"$outputFolder/deploy.Jenkinsfile").toFile

    if (!inputFile.exists()) {
      Left(NonEmptyList.of(ToolFormError(s"Input file [$inputFile] does not exist.")))
    } else if (!inputFile.isFile) {
      Left(NonEmptyList.of(ToolFormError(s"Input file [$inputFile] is not a valid file.")))
    } else if (!buildFile.getParentFile.exists()) {
      Left(NonEmptyList.of(ToolFormError(s"Output directory [${buildFile.getParentFile}] does not exist.")))
    } else {
      for {
        project <- ProjectReader.readProject(inputFile)
        status  <- GenerateJenkinsFilesCommand.runGenerateJenkinsFiles(inputFile.getAbsolutePath, buildFile, deployFile, templateFolderPath, project)
      } yield status
    }
  }
}

object GenerateJenkinsFilesCommand {
  val engine = new TemplateEngine

  /**
    * The main entry point into the Docker Compose files generation.
    *
    * @param sourceFilePath         project config input file path
    * @param buildFile              output file for build.Jenkinsfile.
    * @param deployFile             output file for deploy.Jenkinsfile.
    * @param templateFolderPath     location of where the template files are,
    *                               it must include [[BuildJenkinsfile.templateFileName]] and [[DeployJenkinsfile.templateFileName]] template files.
    * @param project                the abstract project definition parsed by ToolFormApp.
    * @return                       on success it returns a status message to print to the screen, otherwise it will return an
    *                               error object describing what went wrong.
    */
  def runGenerateJenkinsFiles(sourceFilePath: String, buildFile: File, deployFile: File, templateFolderPath: String, project: Project): Either[NonEmptyList[ToolFormError], String] =
    for {
      buildFileStatus  <- writeJenkinsfile(buildFile, project, templateFolderPath, BuildJenkinsfile)
      deployFileStatus <- writeJenkinsfile(deployFile, project, templateFolderPath, DeployJenkinsfile)
    } yield s"$buildFileStatus\n$deployFileStatus"

  def writeJenkinsfile(outFile: File, project: Project, templateFolderPath: String, jenkinsfile: Jenkinsfile): Either[NonEmptyList[ToolFormError], String] = {
    val writer = new BufferedWriter(new FileWriter(outFile, false))
    val buildJenkinsFile = engine.layout(
      s"$templateFolderPath/${jenkinsfile.templateFileName}",
      jenkinsfile.templateMappings(project)
    )

    try {
      writer.write(buildJenkinsFile)
      Right(s"Wrote Build Jenkins files to [$outFile].")
    } finally {
      writer.close()
    }
  }
}
