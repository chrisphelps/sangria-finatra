import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.sutemi",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "SangriaFinatra",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.0.0",
    libraryDependencies += "org.sangria-graphql" %% "sangria-circe" % "1.0.0",
    libraryDependencies += "io.circe" %% "circe-parser" % "0.6.1"
  )
