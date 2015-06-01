organization in ThisBuild := "fr.inria.powerspy"

name := "powerspy.scala"

version in ThisBuild := "1.2"

scalaVersion in ThisBuild := "2.11.6"

scalacOptions in ThisBuild ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-deprecation"
)

parallelExecution in (ThisBuild, Test) := false

// Logging
libraryDependencies in ThisBuild ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.3",
  "org.apache.logging.log4j" % "log4j-core" % "2.3"
)

// Testing
libraryDependencies in ThisBuild ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)
