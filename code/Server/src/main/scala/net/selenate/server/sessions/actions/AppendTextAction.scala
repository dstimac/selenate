package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }

import scala.collection.JavaConversions._


class AppendTextAction(val d: FirefoxDriver)
    extends IAction[SeReqAppendText, SeResAppendText]
    with ActionCommons {

  protected val log = Log(classOf[AppendTextAction])

  def act = { arg =>
    val elementList: Stream[Boolean] =
      inAllWindowsByBy{ address =>
        tryb {
          findElement(arg.method, arg.query).sendKeys(arg.text)
        }
      }

    val isTextAppended = elementList.contains(true)
    if (isTextAppended) {
      new SeResAppendText()
    } else {
      throw new IllegalArgumentException("Element [%s, %s] was not found in any frame!".format(arg.method.toString, arg.query))
    }
//    switchToFrame(arg.windowHandle, arg.framePath.map(_.toInt).toIndexedSeq)
//    val e = findElement(arg.method, arg.query)
//    e.sendKeys(arg.text)
//
//    new SeResAppendText()
  }
}
