organization := "fr.inria.powerspy"

name := "powerspy.scala"

version := "1.0.1"

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

// For using the app in the console mode with sbt, be sure that the lib folder was created.
// Otherwise, use sbt download-bluecove and sbt download-bluecove-gpl before running the application.
val downloadBluecoveLib = TaskKey[File]("download-bluecove")
val downloadBluecoveGplLib = TaskKey[File]("download-bluecove-gpl")

downloadBluecoveLib := {
  val location = baseDirectory.value / "lib" / "bluecove-2.1.0.jar"
  location.getParentFile.mkdirs()
  IO.download(url("https://bluecove.googlecode.com/files/bluecove-2.1.0.jar"), location)
  location
}

downloadBluecoveGplLib := {
  val location = baseDirectory.value / "lib" / "bluecove-gpl-2.1.0.jar"
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
    downloadBluecoveLib.value -> "lib/bluecove-2.1.0.jar",
    downloadBluecoveGplLib.value -> "lib/bluecove-gpl-2.1.0.jar"
  )
}

startYear := Some(2014)

homepage := Some(new URL("https://github.com/Spirals-Team/powerspy.scala"))

licenses := Seq(("AGPL", new URL("http://www.gnu.org/licenses/agpl-3.0.txt")))

pomExtra := {
  <scm>
    <connection>scm:git:github.com/Spirals-Team/powerspy.scala</connection>
    <developerConnection>scm:git:git@github.com:Spirals-Team/powerspy.scala</developerConnection>
    <url>github.com/Spirals-Team/powerspy.scala</url>
  </scm>
    <developers>
      <developer>
        <id>mcolmant</id>
        <name>Maxime Colmant</name>
        <url>http://researchers.lille.inria.fr/colmant/</url>
      </developer>
    </developers>
}

publishMavenStyle := true

sonatypeSettings

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
