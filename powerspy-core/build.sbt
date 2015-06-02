name := "powerspy-core"

// Bluecove (for compilation purpose)
libraryDependencies ++= Seq(
  "net.sf.bluecove" % "bluecove" % "2.1.0" % "provided"
)

startYear := Some(2014)

homepage := Some(url("https://github.com/Spirals-Team/powerspy.scala"))

licenses := Seq("AGPL-3.0" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))

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
      <url>http://mcolmant.github.io/</url>
    </developer>
  </developers>
}

publishMavenStyle := true

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

val downloadBluecoveLibs = TaskKey[Seq[File]]("download-bluecove")

downloadBluecoveLibs := {
  val locationBluecove = baseDirectory.value / "lib" / "bluecove-2.1.0.jar"
  val locationBluecoveGpl = baseDirectory.value / "lib" / "bluecove-gpl-2.1.0.jar"
  if(!locationBluecove.getParentFile.exists()) locationBluecove.getParentFile.mkdirs()
  if(!locationBluecoveGpl.getParentFile.exists()) locationBluecoveGpl.getParentFile.mkdirs()
  if(!locationBluecove.exists()) IO.download(url("https://bluecove.googlecode.com/files/bluecove-2.1.0.jar"), locationBluecove)
  if(!locationBluecoveGpl.exists()) IO.download(url("https://bluecove.googlecode.com/files/bluecove-gpl-2.1.0.jar"), locationBluecoveGpl)
  Seq(locationBluecove, locationBluecoveGpl)
}
