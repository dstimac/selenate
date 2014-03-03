package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }
import scala.collection.JavaConversions._
import org.openqa.selenium.support.ui.Select


class SelectOptionAction(val d: FirefoxDriver)
    extends IAction[SeReqSelectOption, SeResSelectOption]
    with ActionCommons {

  protected val log = Log(classOf[SelectOptionAction])

  def act = { arg =>
    val elementList: Stream[Boolean] =
      inAllWindowsByBy{ address =>
        tryb {
          val e = findElement(arg.parentMethod, arg.parentQuery)
          val s = new Select(e)
          selectOption(s, arg.optionMethod, arg.optionQuery)
        }
      }

    val isElementSelected = elementList.contains(true)
    if (isElementSelected) {
      new SeResSelectOption()
    } else {
      throw new IllegalArgumentException("Element [%s, %s] was not found in any frame!".format(arg.parentMethod.toString, arg.parentQuery))
    }
//    switchToFrame(arg.windowHandle, arg.framePath.map(_.toInt).toIndexedSeq)
//    val e = findElement(arg.parentMethod, arg.parentQuery)
//    val s = new Select(e)
//    selectOption(s, arg.optionMethod, arg.optionQuery)
//
//    new SeResSelectOption()
  }
}
