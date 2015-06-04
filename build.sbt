name := """ChatStats"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "3.0.1"
)

mappings in Universal ++=
  (baseDirectory.value / "datafiles" * "*" get) map
    (x => x -> ("datafiles/" + x.getName))

//do not include API documentation in the generated package
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
