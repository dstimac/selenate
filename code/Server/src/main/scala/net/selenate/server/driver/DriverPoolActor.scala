package net.selenate
package server
package driver

import java.util.UUID

import org.openqa.selenium.remote.RemoteWebDriver

import scala.collection.mutable.Queue
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

private[driver] class DriverPoolActor(val profile: DriverProfile, val size: Int) extends IDriverPoolActor with DriverFactory {
  private case class DriverEntry(uuid: UUID, future: Future[RemoteWebDriver])

  private val log = Log(classOf[DriverPoolActor])
  private val pool = new Queue[DriverEntry]

  log.info("Firing up Firefox Driver Pool Actor. Initial size: %d." format size)
  enqueueNew(size)

  private def enqueueNew(count: Int) {
    for (i <- 1 to count) {
      enqueueNew()
    }
  }

  private def enqueueNew() {
    val uuid = UUID.randomUUID

    val driverFuture = Future {
      log.info("Driver pool actor starting a new entry: {%s}." format uuid)

      val options = DriverOptions(Map.empty, C.Server.gridURL, C.Server.gridPercentage)
      val driver = createDriver(options)
      log.info("Driver pool actor entry {%s} started." format uuid)
      driver
    }
    val driverEntry = DriverEntry(uuid, driverFuture)
    pool.enqueue(driverEntry)
  }

  def get: RemoteWebDriver = {
    enqueueNew()
    val driverEntry = pool.dequeue
    log.info("Driver pool actor waiting for entry {%s}" format driverEntry.uuid)
    val driver = Await.result(driverEntry.future, 30 seconds)
    log.info("Driver pool actor returning entry {%s}" format driverEntry.uuid)
    driver
  }
}
