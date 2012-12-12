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


class ElementAction(val d: FirefoxDriver)
    extends IAction[SeReqElement, SeResElement]
    with ActionCommons {

  def act = { arg =>
    val e = findElement(arg.method, arg.selector).asInstanceOf[RemoteWebElement]
    parseWebElement(e)
  }
}