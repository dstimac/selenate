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
import net.selenate.common.user.BrowserPage

class WaitForBrowserPageAction(val d: FirefoxDriver)
    extends IAction[SeReqWaitForBrowserPage, SeResWaitForBrowserPage]
    with ActionCommons
    with WaitFor {
  protected val log = Log(classOf[WaitForBrowserPageAction])

  def act = { arg =>
    val res = waitForPageList(arg.pageList.toIndexedSeq)

    val ret = res match {
      case Some(x) => toBrowserPage(x);
      case _       => null;
    }

    // TODO SeResWaitForBrowserPage BasePage => SePage and conversion somewhere else
    new SeResWaitForBrowserPage(res.isDefined, ret)
  }


  def waitForPageList(pageList: IndexedSeq[SePage]): Option[SePage] =
    waitForPredicate {
      pageList find pageExists
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
