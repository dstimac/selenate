package net.selenate.server
package driver

import com.instantor.selenium.InstantorWebDriverFactory
import org.openqa.selenium.firefox.{FirefoxProfile, FirefoxDriver}
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}

import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._

trait DriverFactory {
  private val log = Log(classOf[DriverFactory])

  def createDriver(options: DriverOptions): RemoteWebDriver = {
    val c = DesiredCapabilities.firefox()
    options.capabilities foreach { case(k, v) => c.setCapability(k, v)}

    (options.gridURL, options.startInGrid) match {
      case (Some(url), true) =>
        Try {
              InstantorWebDriverFactory.useHub(url).create()
            } match {
          case Success(d) =>
//            log.info(s"########### Starting new Driver instance[$scrapeId] in GRID!")
            d
          case Failure(t) =>
//            log.error(s"Failed to start new remote driver[$scrapeId] on grid!", t)

            new FirefoxDriver(toFirefoxProfile(c))
        }
      case _ =>
//        log.info(s"########### Starting new Driver instance[$scrapeId] on HOST!")
        new FirefoxDriver(toFirefoxProfile(c))
    }
  }

  def toFirefoxProfile(c: DesiredCapabilities): FirefoxProfile = {
    val cc = new FirefoxProfile()
    c.asMap.asScala foreach {
      case (k, i: Int)      => cc.setPreference(k, i)
      case (k, s: String)   => cc.setPreference(k, s)
      case (k, b: Boolean)  => cc.setPreference(k, b)
      case _                => // TODO WARN
    }
    cc
  }

  def createDriver(): RemoteWebDriver = createDriver(new DriverOptions(Map.empty, None, startInGrid = false))
}
