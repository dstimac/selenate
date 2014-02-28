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


class ElementExistsAction(val d: FirefoxDriver)
    extends IAction[SeReqElementExists, SeResElementExists]
    with ActionCommons {

  protected val log = Log(classOf[ElementExistsAction])

  def act = { arg =>
    val resElementList: Stream[Boolean] = inAllWindowsByBy{ address =>
      tryb {
        findElement(arg.method, arg.query)
      }
    }

    val isFound = resElementList.contains(true)
    new SeResElementExists(isFound)
  }
}
