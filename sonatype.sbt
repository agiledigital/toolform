import ReleaseTransformations._
import Release._

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "au.com.agiledigital"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeReleaseAll"),
  updateScripts,
  updateLaunchers,
  commitUpdates,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
