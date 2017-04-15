name := "powerspy-app"

// CLI
libraryDependencies ++= Seq(
  "org.rogach" % "scallop_2.12" % "2.1.1"
)

mappings in Universal ++= {
  val dir = baseDirectory.value.getParentFile

  (for {
    (file, relativePath) <- (dir * "README*" --- dir) pair relativeTo (dir)
  } yield file -> s"$relativePath") ++
    (for {
      (file, relativePath) <- (dir * "LICENSE*" --- dir) pair relativeTo (dir)
    } yield file -> s"$relativePath")
}


// To use for configuring log4j2.
scriptClasspath ++= Seq("../conf")
