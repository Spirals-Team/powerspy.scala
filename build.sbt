name := "powerspy.scala"

lazy val downloadBluecove = taskKey[File]("download-bluecove-app")
lazy val downloadBluecoveGpl = taskKey[File]("download-bluecove-gpl-app")

val shared = Seq(
  organization := "fr.inria.powerspy",
  version := "1.2",
  scalaVersion := "2.12.1",
  scalacOptions := Seq(
    "-language:postfixOps",
    "-feature",
    "-deprecation"
  ),
  parallelExecution := false,
  unmanagedBase := root.base.getAbsoluteFile / "external-libs",
  downloadBluecove := {
    val locationBluecove = root.base.getAbsoluteFile / "external-libs" / "bluecove-2.1.0.jar"
    if (!locationBluecove.exists()) IO.download(url("https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/bluecove/bluecove-2.1.0.jar"), locationBluecove)
    locationBluecove
  },
  downloadBluecoveGpl := {
    val locationBluecoveGpl = root.base.getAbsoluteFile / "external-libs" / "bluecove-gpl-2.1.0.jar"
    if (!locationBluecoveGpl.exists()) IO.download(url("https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/bluecove/bluecove-gpl-2.1.0.jar"), locationBluecoveGpl)
    locationBluecoveGpl
  },
  compile in Compile := (compile in Compile).dependsOn(downloadBluecove, downloadBluecoveGpl).value,
  libraryDependencies ++= Seq(
    "org.apache.logging.log4j" % "log4j-api" % "2.3",
    "org.apache.logging.log4j" % "log4j-core" % "2.3"
  )
)


lazy val root: sbt.Project = (project in file(".")).aggregate(core, cli).settings(shared)

lazy val core = (project in file("powerspy-core")).settings(shared)
lazy val cli = (project in file("powerspy-app")).settings(shared).dependsOn(core % "compile -> compile; test -> test").enablePlugins(JavaAppPackaging)
