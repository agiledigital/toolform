import sbtassembly.AssemblyPlugin._

name := "toolform"

organization := "au.com.agiledigital"

// This version number will be modified by the build system. It should be of the form x.y, not x.y.z
version := "0.1"

scalaVersion := "2.12.2"

assemblySettings

assemblyOption in assembly ~= {
  _.copy(cacheOutput = true)
}

assemblyOption in assembly ~= {
  _.copy(cacheUnzip = true)
}

//Define dependencies.
libraryDependencies ++= Seq(
  "org.scalatest"    %% "scalatest"  % "3.0.1" % "test",
  "org.scalacheck"   %% "scalacheck" % "1.13.4" % "test",
  "com.typesafe"     % "config"      % "1.3.1",
  "com.github.scopt" %% "scopt"      % "3.7.0"
)

// For Settings/Task reference, see http://www.scala-sbt.org/release/sxr/sbt/Keys.scala.html

// Compiler settings. Use scalac -X for other options and their description.
// See Here for more info http://www.scala-lang.org/files/archive/nightly/docs/manual/html/scalac.html
scalacOptions ++= List("-feature", "-deprecation", "-unchecked", "-Xlint")

// ScalaTest settings.
// Ignore tests tagged as @Slow (they should be picked only by integration test)
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.scalatest.tags.Slow", "-u", "target/junit-xml-reports", "-oD", "-eS")

scalafmtOnCompile in ThisBuild := true

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

// To publish, put credentials in ~/.ivy2/credentials
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
