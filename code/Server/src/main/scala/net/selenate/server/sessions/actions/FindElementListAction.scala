package net.selenate
package server
package sessions
package actions

import common.comms.res._
import common.comms.req._

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebElement

import scala.collection.JavaConversions._


class FindElementListAction(val d: FirefoxDriver)
    extends IAction[SeReqFindElementList, SeResFindElementList]
    with ActionCommons {

  protected val log = Log(classOf[FindElementListAction])

  def act = { arg =>
    val resElementList = inAllWindowsByBy{ address =>
      val webElementList = findElementList(arg.method, arg.query)
      webElementList map parseWebElement(address)
    }

    new SeResFindElementList(seqToRealJava(resElementList.flatten))
  }
}
