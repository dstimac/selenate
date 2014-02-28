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
import org.openqa.selenium.Cookie


class AddCookieAction(val d: FirefoxDriver)
    extends IAction[SeReqAddCookie, SeResAddCookie]
    with ActionCommons {

  protected val log = Log(classOf[AddCookieAction])

  def act = { arg =>
    val cookie = arg.cookie
    inAllWindowsByBy{ address =>
      // convert from selenate cookie to selenium cookie
      val c = new Cookie(
            cookie.getName
          , cookie.getValue
          , cookie.getDomain
          , cookie.getPath
          , cookie.getExpiry
          , cookie.isSecure
          )
      d.manage.addCookie(c)
    }
    new SeResAddCookie()
  }
}
