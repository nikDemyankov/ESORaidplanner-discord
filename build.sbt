import Dependencies._

lazy val common = Seq(
  organization := "net.wayfarerx.esoraidplanner",
  scalaVersion := "2.12.1",
  version := "0.1.0-SNAPSHOT"/*,
  test in assembly := {}*/
)

lazy val discord = (project in file("discord")).settings(
  common,

  name := "esoraidplanner-discord",

  libraryDependencies += akka,
  libraryDependencies += discord4j,
  libraryDependencies += commonsIO,
  libraryDependencies += circe,
  libraryDependencies += circeGeneric,
  libraryDependencies += circeParser,
  libraryDependencies += circeExtras,
  libraryDependencies += logback,

  libraryDependencies += akkaTest,
  libraryDependencies += scalaTest/*,

  mainClass in assembly := Some("net.wayfarerx.esoraidplanner.discord.main.Program"),
  assemblyJarName in assembly := "esoraidplanner-discord.jar"*/

)
