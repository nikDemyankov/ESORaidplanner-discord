import Dependencies._

lazy val root = (project in file(".")).settings(
  organization := "net.wayfarerx.esoraidplanner",
  name := "esoraidplanner-discord",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  libraryDependencies ++= Seq(
    Discord4j,
    Http4sBlazeServer,
    Http4sCirce,
    Http4sDsl,
    Logback,
    ScalaTest
  )
)
