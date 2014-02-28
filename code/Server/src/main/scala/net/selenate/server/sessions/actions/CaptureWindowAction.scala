package net.selenate
package server
package sessions
package actions

import common.comms._
import res._
import req._
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{ By, WebElement }
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io._

class CaptureWindowAction(val d: FirefoxDriver)
    extends IAction[SeReqCaptureWindow, SeResCaptureWindow]
    with ActionCommons {

  protected val log = Log(classOf[CaptureWindowAction])

  def act = { arg =>
    val resScreenshotList: Stream[Option[SeResCaptureWindow]] = inAllWindowsByBy{ address =>
      tryo {
        val webElement   = findElement(arg.method, arg.query)
        val baScreenshot = getScreenshot()
        new SeResCaptureWindow(baScreenshot)
      }
    }

    val e = resScreenshotList.flatten
    if (e.isEmpty) {
      throw new IllegalArgumentException("Couldn't take screenshot." +
          "Element [%s, %s] was not found in any frame!".format(arg.method.toString, arg.query))
    } else {
      e(0)
    }
  }

  private def getScreenshot(): Array[Byte] = {

    val html2Canvasplugin = IOUtils.toString(getClass().getResourceAsStream("html2canvas.min.js"))

    log.debug("Injecting html2canvas.min.js...")
    d.executeScript(appendScript(html2Canvasplugin))

    log.debug("Injecting canvas to the body")
    d.executeScript("""html2canvas(document.body, {
        onrendered: function(canvas) {
            document.body.appendChild(canvas);
          }
      });
      """)

    val base64Img = d.executeScript("""return document.getElementsByTagName('canvas')[0].toDataURL();""")

    Base64.decodeBase64(base64Img.toString.replace("data:image/png;base64,", ""))
  }

  private def appendScript(jsplugin: String) = """
    var script = document.createElement(script);
    script.innerHTML = %s;
    document.body.appendChild(script);""" format jsplugin
}