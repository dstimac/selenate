package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }

import scala.collection.JavaConversions._

class ClearTextAction(val d: FirefoxDriver)
    extends IAction[SeReqClearText, SeResClearText]
    with ActionCommons {

  protected val log = Log(classOf[ClearTextAction])

  def act = { arg =>

    val elementList: Stream[Boolean] =
      inAllWindowsByBy{ address =>
        tryb {
          findElement(arg.method, arg.query).clear
        }
      }

    val isTextClear = elementList.contains(true)
    if (isTextClear) {
      new SeResClearText()
    } else {
      throw new IllegalArgumentException("Element [%s, %s] was not found in any frame!".format(arg.method.toString, arg.query))
    }

//    switchToFrame(arg.windowHandle, arg.framePath.map(_.toInt).toIndexedSeq)
//    val e = findElement(arg.method, arg.query)
//    e.clear
//
//    new SeResClearText()
  }
}
