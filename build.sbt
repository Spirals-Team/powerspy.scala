organization in ThisBuild := "fr.inria.powerspy"

name := "powerspy.scala"

version in ThisBuild := "1.1"

scalaVersion in ThisBuild := "2.11.4"

scalacOptions in ThisBuild ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-deprecation"
)

parallelExecution in (ThisBuild, Test) := false
