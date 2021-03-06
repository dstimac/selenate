package net.selenate.server
package actions
package workers

import extensions.SelenateFirefox
import linux.LinuxWindow

import net.selenate.common.comms.req.SeReqSystemClick
import net.selenate.common.comms.res.SeResSystemClick
import net.selenate.common.NamedUUID

class SystemClickAction(val sessionID: String, val context: SessionContext, val d: SelenateFirefox)
    extends Action[SeReqSystemClick, SeResSystemClick]
    with ActionCommons {
  private val titleUuidFactory = new NamedUUID("Title")

  def doAct = { arg =>
    val title = titleUuidFactory.random
    d.title = title
    LinuxWindow.clickRelative(d.displayInfo.map(_.num), title, arg.getX, arg.getY)
    new SeResSystemClick()
  }
}
