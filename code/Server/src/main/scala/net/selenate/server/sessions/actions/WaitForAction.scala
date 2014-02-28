package net.selenate
package server
package sessions
package actions

import common.comms._
import res._
import req._

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }

import scala.collection.JavaConversions._

class WaitForAction(val d: FirefoxDriver)
    extends IAction[SeReqWaitFor, SeResWaitFor]
    with ActionCommons
    with WaitFor {
  protected val log = Log(classOf[WaitForAction])

  def act = { arg =>
    val res = waitForPageList(arg.pageList.toIndexedSeq)

    new SeResWaitFor(res.isDefined, res.orNull)
  }


  def waitForPageList(pageList: IndexedSeq[SePage]): Option[String] =
    waitForPredicate {
      pageList find pageExists map (_.name)
    }

  def pageExists(page: SePage): Boolean =
    (page.selectorList map elementExists).foldLeft(true)(_ && _)

  def elementExists(selector: SeElementSelector): Boolean =
    try {
      val elemOpt = inAllWindowsByBy{ address =>
        tryo {
          findElement(selector.method, selector.query)
        }
      }
      elemOpt.exists(_.isDefined)
    } catch {
      case e: Exception => false
    }
}
