package net.selenate.server.driver

import java.net.URL

object DriverOptions {
  def apply(c: Map[String, Any], gridURL: Option[String], gridPercentage: Option[Int]) = {
    val startInGrid = scala.util.Random.nextInt(100) < gridPercentage.getOrElse(-1)

    gridURL match {
      case Some(url) => new DriverOptions(c, Some(new java.net.URL(url)), startInGrid )
      case _         => new DriverOptions(c, None, startInGrid = false)
    }
  }
}

class DriverOptions(val capabilities: Map[String, Any], val gridURL: Option[URL], val startInGrid: Boolean)