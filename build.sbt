// build.sbt --- Scala build tool settings

scalaVersion := "2.12.1"

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
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.12.0",
  "org.slf4j" % "slf4j-nop" % "1.7.21",
  "org.json4s" %% "json4s-native" % "3.4.2"
)
