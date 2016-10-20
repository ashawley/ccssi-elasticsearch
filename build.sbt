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
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.2",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
