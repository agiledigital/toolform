import sbtassembly.AssemblyPlugin._

name := "toolform"

organization := "au.com.agiledigital"

scalaVersion := "2.12.4"

assemblySettings

assemblyOption in assembly ~= {
  _.copy(cacheOutput = true)
}

assemblyOption in assembly ~= {
  _.copy(cacheUnzip = true)
}

val pureconfigVersion = "0.8.0"

//Define dependencies.
libraryDependencies ++= Seq(
  "org.scalatest"         %% "scalatest"             % "3.0.1" % "test",
  "org.scalacheck"        %% "scalacheck"            % "1.13.4" % "test",
  "com.typesafe"          % "config"                 % "1.3.1",
  "com.github.pureconfig" %% "pureconfig"            % pureconfigVersion,
  "com.github.pureconfig" %% "pureconfig-enumeratum" % pureconfigVersion,
  "com.monovore"          %% "decline"               % "0.3.0",
  "com.beachape"          %% "enumeratum"            % "1.5.12",
  "org.typelevel"         %% "cats-core"             % "0.9.0",
  "org.scalatra.scalate"  %% "scalate-core"          % "1.9.0"
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

trapExit := false

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
