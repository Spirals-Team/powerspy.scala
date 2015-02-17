name := "powerspy-app"

// Logging
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.1"
)

// CLI
libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "0.9.5"
)

// For using the application, be sure that the lib folder was created.
// Otherwise, use sbt download-bluecove before running the application.
val downloadBluecoveLibs = TaskKey[Seq[File]]("download-bluecove")

downloadBluecoveLibs := {
  val locationBluecove = baseDirectory.value / "lib" / "bluecove-2.1.0.jar"
  val locationBluecoveGpl = baseDirectory.value / "lib" / "bluecove-gpl-2.1.0.jar"
  locationBluecove.getParentFile.mkdirs()
  locationBluecoveGpl.getParentFile.mkdirs()
  IO.download(url("https://bluecove.googlecode.com/files/bluecove-2.1.0.jar"), locationBluecove)
  IO.download(url("https://bluecove.googlecode.com/files/bluecove-gpl-2.1.0.jar"), locationBluecoveGpl)
  Seq(locationBluecove, locationBluecoveGpl)
}

name in Universal := "powerspy-app"

mappings in Universal ++= {
  ((file("../") * "README*").get map {
    readmeFile: File =>
      readmeFile -> readmeFile.getName
  }) ++
  ((file("../") * "LICENSE*").get map {
    licenseFile: File =>
      licenseFile -> licenseFile.getName
  })
}

// To use for configuring log4j2.
scriptClasspath ++= Seq("../conf")
