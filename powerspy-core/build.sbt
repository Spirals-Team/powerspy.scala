name := "powerspy-core"

// Bluecove (for compilation purpose)
libraryDependencies ++= Seq(
  "net.sf.bluecove" % "bluecove" % "2.1.0" % "provided"
)

// Testing
libraryDependencies ++= Seq(
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
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
