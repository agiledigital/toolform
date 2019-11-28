import sbtassembly.AssemblyPlugin._

name := "toolform"

organization := "au.com.agiledigital"

//crossScalaVersions := Seq("2.13.1", "2.12.4")
//scalaVersion := crossScalaVersions.value.head

scalaVersion := "2.13.1"

assemblySettings

assemblyOption in assembly ~= {
  _.copy(cacheOutput = true)
}

assemblyOption in assembly ~= {
  _.copy(cacheUnzip = true)
}

val pureconfigVersion = "0.12.1" //didn't work with 0.11.1 but works with this strangely

//Define dependencies.
libraryDependencies ++= Seq(
  "org.scalatest"         %% "scalatest"             % "3.2.0-M1" % "test", //"3.0.1" % "test",
  "org.scalacheck"        %% "scalacheck"            % "1.14.2" % "test", //"1.13.4" % "test",
  "com.typesafe"          % "config"                 % "1.3.1",
  "com.github.pureconfig" %% "pureconfig"            % pureconfigVersion,
  "com.github.pureconfig" %% "pureconfig-enumeratum" % pureconfigVersion,
  "com.monovore"          %% "decline"               % "1.0.0", //"0.3.0",
  "com.beachape"          %% "enumeratum"            % "1.5.12",
  "org.typelevel"         %% "cats-core"             % "0.9.0"
)

// For Settings/Task reference, see http://www.scala-sbt.org/release/sxr/sbt/Keys.scala.html

// Compiler settings. Use scalac -X for other options and their description.
// See Here for more info http://www.scala-lang.org/files/archive/nightly/docs/manual/html/scalac.html
scalacOptions ++= List("-feature", "-deprecation", "-unchecked", "-Xlint")
scalacOptions += "-Xmacro-settings:materialize-derivations"

// ScalaTest settings.
// Ignore tests tagged as @Slow (they should be picked only by integration test)
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.scalatest.tags.Slow", "-u", "target/junit-xml-reports", "-oD", "-eS")

scalafmtOnCompile in ThisBuild := true

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "au.com.agiledigital.toolform.version"
  )
