package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }
import scala.collection.JavaConversions._
import org.openqa.selenium.Keys

class InputTextAction(val d: FirefoxDriver)(implicit context: ActionContext)
    extends IAction[SeReqInputText, SeResInputText]
    with ActionCommons {

  protected val log = Log(classOf[InputTextAction])

  def act = { arg =>
    switchToFrame(arg.windowHandle, arg.framePath.map(_.toInt).toIndexedSeq)
    val e = findElement(arg.method, arg.query)

    e.sendKeys(Keys.chord(arg.text))
    e.sendKeys(Keys.ESCAPE) // only the special keys will trigger the keyUp event

    new SeResInputText()
  }
}
