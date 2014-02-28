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


class FindAndClickAction(val d: FirefoxDriver)
    extends IAction[SeReqFindAndClick, SeResFindAndClick]
    with ActionCommons {
  type PathElement = (FramePath, RemoteWebElement)

  protected val log = Log(classOf[FindAndClickAction])

  def act = { arg =>
    val elementList: Stream[Boolean] =
      inAllWindowsByBy{ address =>
        tryb {
          findElement(arg.method, arg.query).click
        }
      }

    val isElementClicked = elementList.contains(true)
    if (isElementClicked) {
      new SeResFindAndClick()
    } else {
      throw new IllegalArgumentException("Element [%s, %s] was not found in any frame!".format(arg.method.toString, arg.query))
    }
  }
}
