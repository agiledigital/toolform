resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.9.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

// For formatting of the source code.
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.8")