import sbt._
import Keys._

trait Dependencies {
  val akkaVersion = "2.4.0"
  val akkaActor   = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  val akkaRemote  = "com.typesafe.akka" %% "akka-remote" % akkaVersion
  val akkaSlf4j   = "com.typesafe.akka" %% "akka-slf4j"  % akkaVersion

  val jodaTimeConvert = "org.joda"  % "joda-convert" % "1.8.1"
  val jodaTimeTime    = "joda-time" % "joda-time"    % "2.8.2"

  val seleniumVersion = "2.48.2"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion
  val seleniumServer  = "org.seleniumhq.selenium" % "selenium-server"         % seleniumVersion

  // ---------------------------------------------------------------------------
  
  val config           = "com.typesafe"            %  "config"               % "1.3.0"
  val dispatch         = "net.databinder.dispatch" %% "dispatch-core"        % "0.11.3"
  val logback          = "ch.qos.logback"          %  "logback-classic"      % "1.1.3"
  val slf4j            = "org.slf4j"               %  "slf4j-api"            % "1.7.12"
  val procrun          = "com.ferega.procrun"      %% "processrunner"        % "0.1.3"

  val akka     = Seq(akkaActor, akkaRemote, akkaSlf4j)
  val jodaTime = Seq(jodaTimeConvert, jodaTimeTime)
  val selenium = Seq(seleniumFirefox, seleniumServer)
}
