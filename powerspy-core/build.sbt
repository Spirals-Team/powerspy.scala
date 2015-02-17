name := "powerspy-core"

// Logging
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.1"
)

// Bluecove (for compilation purpose)
libraryDependencies ++= Seq(
  "net.sf.bluecove" % "bluecove" % "2.1.0" % "provided"
)

// Tests
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
)

startYear := Some(2014)

homepage := Some(new URL("https://github.com/Spirals-Team/powerspy.scala"))

licenses := Seq(("AGPL-3.0", new URL("http://www.gnu.org/licenses/agpl-3.0.txt")))

pomExtra := {
  <url>https://github.com/Spirals-Team/powerspy.scala</url>
  <licenses>
    <license>
      <name>AGPL-3.0</name>
      <url>http://www.gnu.org/licenses/agpl-3.0.txt</url>
    </license>
  </licenses>
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
