import sbt._
import Keys._

object Dependencies {
  val akkaVersion = "2.1.2"
  val akkaActor   = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  val akkaRemote  = "com.typesafe.akka" %% "akka-remote" % akkaVersion

  val jodaTimeConvert = "org.joda"  % "joda-convert" % "1.3.1"
  val jodaTimeTime    = "joda-time" % "joda-time"    % "2.2"

  val seleniumVersion = "2.40.0"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion
  val seleniumServer  = "org.seleniumhq.selenium" % "selenium-server"         % seleniumVersion

  val scalaLibrary    = "org.scala-lang" % "scala-library" % "2.10.1"

  // ---------------------------------------------------------------------------

  val configrity = "org.streum"              %% "configrity-core" % "1.0.0"
  val dispatch   = "net.databinder.dispatch" %% "dispatch-core"   % "0.10.0"
  val logback    = "ch.qos.logback"          %  "logback-classic" % "1.0.13"
  val scalaTest  = "org.scalatest"           %% "scalatest"       % "1.9.1"
  val slf4j      = "org.slf4j"               %  "slf4j-api"       % "1.7.5"

  val akka     = Seq(akkaActor, akkaRemote)
  val jodaTime = Seq(jodaTimeConvert, jodaTimeTime)
  val selenium = Seq(seleniumFirefox, seleniumServer)
}
