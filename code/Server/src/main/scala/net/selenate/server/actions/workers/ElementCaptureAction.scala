package net.selenate.server
package actions
package workers

import extensions.SelenateFirefox

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import javax.imageio.ImageIO
import net.selenate.common.comms.req.SeReqElementCapture
import net.selenate.common.comms.res.SeResElementCapture
import net.selenate.common.exceptions.SeActionException
import org.openqa.selenium.OutputType
import org.openqa.selenium.remote.RemoteWebElement
import scala.util.{ Failure, Success, Try }

class ElementCaptureAction(val sessionID: String, val context: SessionContext, val d: SelenateFirefox)
    extends Action[SeReqElementCapture, SeResElementCapture]
    with ActionCommons {
  def doAct = { arg =>
    val result: Option[Try[Array[Byte]]] =
      fromCache(arg) orElse fromFrames(arg)

    result match {
      case Some(Success((r))) =>
        new SeResElementCapture(r)
      case Some(Failure(ex)) =>
        logError(s"An error occurred while executing $name action ($arg)!", ex)
        throw new SeActionException(name, arg, ex)
      case None =>
        val msg = "element not found in any frame"
        logError(s"An error occurred while executing $name action ($arg): $msg!")
        throw new SeActionException(name, arg, msg)
    }
  }

  def fromCache(arg: SeReqElementCapture): Option[Try[Array[Byte]]] =
    elementInCache(arg.getSelector) { (address, elem) =>
      doCapture(elem)
    }

  def fromFrames(arg: SeReqElementCapture): Option[Try[Array[Byte]]] =
    elementInAllWindows(arg.getSelector) { (address, elem) =>
      doCapture(elem)
    }

  def doCapture(elem: RemoteWebElement): Array[Byte] = {
    val screen     = d.getScreenshotAs(OutputType.BYTES)
    val bais       = new ByteArrayInputStream(screen)
    val baos       = new ByteArrayOutputStream()
    val img        = ImageIO.read(bais)
    val dest       = img.getSubimage(elem.getLocation.getX, elem.getLocation.getY, elem.getSize.getWidth, elem.getSize.getHeight)
    ImageIO.write(dest, "png", baos)
    val screenBody = baos.toByteArray()
    baos.close()
    bais.close()
    screenBody
  }
}
