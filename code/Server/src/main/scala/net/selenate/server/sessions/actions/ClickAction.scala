package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }
import scala.collection.JavaConversions._
import java.io.IOException

class ClickAction(val d: FirefoxDriver)
    extends IAction[SeReqClick, SeResClick]
    with ActionCommons {

  protected val log = Log(classOf[ClickAction])

  def act = { arg =>
    val elementList: Stream[Boolean] =
      inAllWindowsByBy{ address =>
        tryb {
          findElement(arg.method, arg.query).click
        }
      }

    val isElementClicked = elementList.contains(true)
    if (isElementClicked) {
      new SeResClick()
    } else {
      throw new IllegalArgumentException("Element [%s, %s] was not found in any frame!".format(arg.method.toString, arg.query))
    }
//    switchToFrame(arg.windowHandle, arg.framePath.map(_.toInt).toIndexedSeq)
//    val e = findElement(arg.method, arg.query)
//    e.click
  }
}
