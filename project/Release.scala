import java.nio.charset.StandardCharsets
import java.nio.file.Files

import sbt.Keys.baseDirectory
import sbt._
import sbtrelease.ReleasePlugin.autoImport._

import scala.io.{Codec, Source}
import scala.util.matching.Regex

/**
  * Code here borrowed heavily from the https://github.com/coursier/coursier build itself.
  *
  */
object Release {

  implicit final class StateOps(val state: State) extends AnyVal {
    def vcs: sbtrelease.Vcs =
      Project.extract(state).get(releaseVcs).getOrElse {
        sys.error("VCS not set")
      }
  }

  val updateLaunchers = ReleaseStep { state =>
    val baseDir    = Project.extract(state).get(baseDirectory.in(ThisBuild))
    val scriptsDir = baseDir / "scripts"
    val scriptFiles = Seq(
      (scriptsDir / "generate-launcher.sh") -> (baseDir / "toolform")
    )

    val vcs = state.vcs

    for ((f, output) <- scriptFiles) {
      scala.sys.process.Process(Seq(f.getAbsolutePath, "-f")).!!(state.log)
      vcs.add(output.getAbsolutePath).!!(state.log)
    }

    state
  }

  val updateScripts = ReleaseStep { state =>
    val (releaseVer, _) = state.get(ReleaseKeys.versions).getOrElse {
      sys.error(s"${ReleaseKeys.versions.label} key not set")
    }

    val scriptsDir = Project.extract(state).get(baseDirectory.in(ThisBuild)) / "scripts"
    val scriptFiles = Seq(
      scriptsDir / "generate-launcher.sh"
    )

    val vcs = state.vcs

    for (f <- scriptFiles) {
      updateVersionInScript(f, releaseVer)
      vcs.add(f.getAbsolutePath).!!(state.log)
    }

    state
  }

  val updateVersionPattern: Regex = "(?m)^VERSION=.*$".r
  def updateVersionInScript(file: File, newVersion: String): Unit = {
    val content = Source.fromFile(file)(Codec.UTF8).mkString

    updateVersionPattern.findAllIn(content).toVector match {
      case Seq()  => sys.error(s"Found no matches in $file")
      case Seq(_) =>
      case _      => sys.error(s"Found too many matches in $file")
    }

    val newContent = updateVersionPattern.replaceAllIn(content, "VERSION=" + newVersion)
    Files.write(file.toPath, newContent.getBytes(StandardCharsets.UTF_8))
  }

  val commitUpdates = ReleaseStep(
    action = { state =>
      val (releaseVer, _) = state.get(ReleaseKeys.versions).getOrElse {
        sys.error(s"${ReleaseKeys.versions.label} key not set")
      }

      state.vcs.commit(s"Updates for $releaseVer", sign = true).!(state.log)

      state
    },
    check = { state =>
      val vcs = state.vcs

      if (vcs.hasModifiedFiles)
        sys.error("Aborting release: unstaged modified files")

      if (vcs.hasUntrackedFiles && !Project.extract(state).get(releaseIgnoreUntrackedFiles))
        sys.error(
          "Aborting release: untracked files. Remove them or specify 'releaseIgnoreUntrackedFiles := true' in settings"
        )

      state
    }
  )
}
