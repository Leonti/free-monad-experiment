import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    resolvers += Resolver.bintrayRepo("projectseptemberinc", "maven"),
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Ypartial-unification", "-encoding", "utf8"),
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.typelevel"   %% "cats" % "0.9.0",
    libraryDependencies += "com.projectseptember" %% "freek" % "0.6.7",
    addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary)
  )
