package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._
import java.util.ArrayList
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebElement
import scala.collection.JavaConversions._


class FindElementAction(val d: FirefoxDriver)
    extends IAction[SeReqFindElement, SeResFindElement]
    with ActionCommons {

  protected val log = Log(classOf[FindElementAction])

  def act = { arg =>
    val resElementList: Stream[Option[SeResFindElement]] = inAllWindowsByBy{ address =>
      tryo {
        val webElement = findElement(arg.method, arg.query)
        val resElement = parseWebElement(address)(webElement)
        new SeResFindElement(resElement)
      }
    }

    val e = resElementList.flatten
    if (e.isEmpty) {
      throw new IllegalArgumentException("Element [%s, %s] was not found in any frame!".format(arg.method.toString, arg.query))
    } else {
      e(0)
    }
  }
}
