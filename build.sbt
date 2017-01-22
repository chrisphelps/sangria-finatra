import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.sutemi",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "SangriaFinatra",
    libraryDependencies += scalaTest % Test,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "Twitter Maven" at "https://maven.twttr.com"
    ),
    libraryDependencies ++= List(
      "org.sangria-graphql" %% "sangria" % "1.0.0",
      "org.sangria-graphql" %% "sangria-circe" % "1.0.0",
      "io.circe" %% "circe-parser" % "0.6.1",
      "com.twitter" %% "finatra-http" % "2.7.0",
      "ch.qos.logback" % "logback-classic" % "1.1.7")
  )
