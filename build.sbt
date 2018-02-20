name := "powerspy.scala"

val shared = Seq(
  organization := "fr.inria.powerspy",
  version := "1.2.1",
  scalaVersion := "2.12.4",
  scalacOptions := Seq(
    "-language:postfixOps",
    "-feature",
    "-deprecation"
  ),
  parallelExecution := false,
  libraryDependencies ++= Seq(
    "org.apache.logging.log4j" % "log4j-api" % "2.10.0",
    "org.apache.logging.log4j" % "log4j-core" % "2.10.0"
  )
)

lazy val root: sbt.Project = (project in file(".")).aggregate(core, cli).settings(shared)
lazy val core = (project in file("powerspy-core")).settings(shared)
lazy val cli = (project in file("powerspy-app")).settings(shared).dependsOn(core % "compile -> compile; test -> test").enablePlugins(JavaAppPackaging)
