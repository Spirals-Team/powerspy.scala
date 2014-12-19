name := "powerspy.scala"

version := "1.0"

scalaVersion := "2.11.4"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging)

// Logging
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.1"
)

// Bluecove (for compilation purpose)
libraryDependencies ++= Seq(
  "net.sf.bluecove" % "bluecove" % "2.1.0" % "provided"
)

// CLI
libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "0.9.5"
)

// Tests
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
)

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-deprecation"
)

parallelExecution in Test := false

val downloadBluecoveLib = TaskKey[File]("Downloads the bluecove runtime dependencies")
val downloadBluecoveGplLib = TaskKey[File]("Downloads the bluecove-gpl runtime dependencies")

downloadBluecoveLib := {
  val location = target.value / "bluecove" / "bluecove-2.1.0.jar"
  location.getParentFile.mkdirs()
  IO.download(url("https://bluecove.googlecode.com/files/bluecove-2.1.0.jar"), location)
  location
}

downloadBluecoveGplLib := {
  val location = target.value / "bluecove" / "bluecove-gpl-2.1.0.jar"
  location.getParentFile.mkdirs()
  IO.download(url("https://bluecove.googlecode.com/files/bluecove-gpl-2.1.0.jar"), location)
  location
}

mappings in Universal ++= {
  ((baseDirectory.value * "README*").get map {
    readmeFile: File =>
      readmeFile -> readmeFile.getName
  }) ++
  ((baseDirectory.value * "LICENSE*").get map {
    licenseFile: File =>
      licenseFile -> licenseFile.getName
  })
}

mappings in Universal ++= {
  Seq(
    downloadBluecoveLib.value -> "bluecove/bluecove-2.1.0.jar",
    downloadBluecoveGplLib.value -> "bluecove/bluecove-gpl-2.1.0.jar"
  )
}

bashScriptExtraDefines += """addJava "-Djava.ext.dirs=${app_home}/../bluecove/""""
