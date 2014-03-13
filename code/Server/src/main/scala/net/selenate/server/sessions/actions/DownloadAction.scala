package net.selenate.server
package sessions.actions

import extensions.SelenateFirefox

import com.ning.http.client.Cookie
import dispatch._
import java.io.IOException
import net.selenate.common.comms.req.SeReqDownload
import net.selenate.common.comms.res.SeResDownload
import scala.collection.JavaConversions._

class DownloadAction(val d: SelenateFirefox) extends IAction[SeReqDownload, SeResDownload] {
  protected val log = Log(classOf[DownloadAction])

  def act = { arg =>
    val request = url(arg.url)
    request.setHeader("Referer", d.getCurrentUrl)
    request.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/12.0")
    getCookies foreach request.addCookie

    val body =
      Http(request OK as.Bytes).either.apply match {
        case Left(e) =>
          throw new IOException("An error occured while downloading the specified URL (%s)." format arg.url, e)
        case Right(body) =>
          body
      }

    new SeResDownload(body)
  }

  private def getCookies =
    d.manage.getCookies.map { c =>
      new Cookie(c.getDomain, c.getName, c.getValue, c.getPath, expiryToInt(c.getExpiry), c.isSecure)
  }

  private def expiryToInt(expiry: java.util.Date) =
    if (expiry == null) {
      -1
    } else {
      (expiry.getTime / 1000).toInt
    }
}
