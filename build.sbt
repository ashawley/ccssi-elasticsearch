// build.sbt --- Scala build tool settings

scalaVersion := "2.11.8" // "2.12.0-M5"

// https://tpolecat.github.io/2014/04/11/scalac-flags.html
scalacOptions in (Compile) ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-Yno-predef",
  "-Yno-imports"
)

libraryDependencies ++= List(
  "org.scalactic" %% "scalactic" % "2.2.6",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.4-SNAPSHOT",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.3",
  "org.json4s" %% "json4s-native" % "3.2.11" withSources(),
  "org.json4s" %% "json4s-jackson" % "3.2.11"
)
