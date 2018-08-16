import Dependencies._

lazy val root = (project in file(".")).settings(
  resolvers += "Spring Lib Release Repository" at "http://repo.spring.io/libs-release/",
  organization := "net.wayfarerx.esoraidplanner",
  name := "esoraidplanner-discord",
  version := "0.1.0",
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq("-feature"),
  test in assembly := {},
  mainClass in assembly := Some("net.wayfarerx.esoraidplanner.discord.main.Program"),
  assemblyJarName in assembly := "esoraidplanner-discord.jar",
  libraryDependencies ++= Seq(
    Discord4j,
    Http4sCirce,
    Http4sDsl,
    Http4sBlazeClient,
    Http4sBlazeServer,
    Logback,
    ScalaTest
  )
)
