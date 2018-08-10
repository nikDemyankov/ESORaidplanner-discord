import sbt._

object Dependencies {

  val Discord4jVersion = "2.10.1"
  lazy val Discord4j = "com.discord4j" % "Discord4J" % Discord4jVersion

  val Http4sVersion = "0.19.0-M1"
  lazy val Http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
  lazy val Http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
  lazy val Http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  val LogbackVersion = "1.2.3"
  lazy val Logback = "ch.qos.logback" % "logback-classic" % LogbackVersion

  val ScalaTestVersion = "3.0.5"
  lazy val ScalaTest = "org.scalatest" %% "scalatest" % ScalaTestVersion % Test

}
