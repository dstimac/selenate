package net.selenate
package server
package sessions

import actions._
import actors.ActorFactory
import common.comms.req._
import common.comms.res._
import driver.{ DriverPool, DriverProfile }
import akka.actor.{ Actor, Cancellable }
import net.selenate.common.comms.req.SeReqDownload
import org.openqa.selenium.firefox.FirefoxProfile
import scala.concurrent.duration.Duration
import net.selenate.common.comms.req._
import akka.actor.PoisonPill

class SessionActor(sessionID: String, profile: DriverProfile, useFrames: Boolean = true) extends Actor {
  private val log  = Log(classOf[SessionActor], sessionID)

  log.info("Creating session actor for session id: {%s}." format sessionID)
  private val d = {
    val d = DriverPool.get(profile)
    d.manage().window().maximize()
    d
  }
  private var keepaliveScheduler: Option[Cancellable] = None
  private def isKeepalive = keepaliveScheduler.isDefined
  implicit val actionContext = ActionContext(useFrames)

  private def actionMan: PF[SeCommsReq, SeCommsRes] = {
    case arg: SeReqAddCookie          => new AddCookieAction(d).act(arg)
    case arg: SeReqAppendText         => new AppendTextAction(d).act(arg)
    case arg: SeReqCapture            => new CaptureAction(d).act(arg)
    case arg: SeReqCaptureElement     => new CaptureElementAction(d).act(arg)
    case arg: SeReqCaptureWindow      => new CaptureWindowAction(d).act(arg)
    case arg: SeReqClearText          => new ClearTextAction(d).act(arg)
    case arg: SeReqClick              => new ClickAction(d).act(arg)
    case arg: SeReqClose              => new CloseAction(d).act(arg)
    case arg: SeReqDeleteCookieNamed  => new DeleteCookieNamedAction(d).act(arg)
    case arg: SeReqDownload           => new DownloadAction(d).act(arg)
    case arg: SeReqDownloadFile       => new DownloadFileAction(d, sessionID).act(arg)
    case arg: SeReqElementExists      => new ElementExistsAction(d).act(arg)
    case arg: SeReqExecuteScript      => new ExecuteScriptAction(d).act(arg)
    case arg: SeReqFindAlert          => new FindAlertAction(d).act(arg)
    case arg: SeReqFindAndClick       => new FindAndClickAction(d).act(arg)
    case arg: SeReqFindElement        => new FindElementAction(d).act(arg)
    case arg: SeReqFindElementList    => new FindElementListAction(d).act(arg)
    case arg: SeReqFindSelect         => new FindSelectAction(d).act(arg)
    case arg: SeReqGet                => new GetAction(d).act(arg)
    case arg: SeReqInputText          => new InputTextAction(d).act(arg)
    case arg: SeReqNavigateBack       => new NavigateBackAction(d).act(arg)
    case arg: SeReqNavigateForward    => new NavigateForwardAction(d).act(arg)
    case arg: SeReqNavigateRefresh    => new NavigateRefreshAction(d).act(arg)
    case arg: SeReqQuit               => new QuitAction(d).act(arg)
    case arg: SeReqResetFrame         => new ResetFrameAction(d).act(arg)
    case arg: SeReqSelectOption       => new SelectOptionAction(d).act(arg)
    case arg: SeReqSetUseFrames       => new SetUseFramesAction(d).act(arg)
    case arg: SeReqSikuliClick        => new SikuliClickAction(d).act(arg)
    case arg: SeReqSikuliImageExists  => new SikuliImageExistsAction(d).act(arg)
    case arg: SeReqSikuliInputText    => new SikuliInputTextAction(d).act(arg)
    case arg: SeReqSikuliTakeScreenshot => new SikuliTakeScreenshotAction(d).act(arg)
    case arg: SeReqStartKeepalive     => new StartKeepaliveAction(d).act(arg)
    case arg: SeReqStopKeepalive      => new StopKeepaliveAction(d).act(arg)
    case arg: SeReqSwitchFrame        => new SwitchFrameAction(d).act(arg)
    case arg: SeReqWaitFor            => new WaitForAction(d).act(arg)
    case arg: SeReqWaitForBrowserPage => new WaitForBrowserPageAction(d).act(arg)
  }

  private def receiveBase: Receive = {
    case "ping" => sender ! "pong"
    case data @ KeepaliveData(delay, reqList) =>
      log.info("Keepalive tick!")
      data.reqList foreach actionMan
    case arg: SeReqStartKeepalive =>
      sender ! actionMan(arg)
      startKeepalive(KeepaliveData.fromReq(arg))
    case arg: SeReqSetUseFrames =>
      actionContext.useFrames = arg.useFrames
      sender ! actionMan(arg)
    case arg: SeCommsReq =>
      stopKeepalive
      sender ! actionMan(arg)
  }


  override def postStop() {
    log.info(s"Post stop for $sessionID, killing browser and actor.")
    deleteProfileFolder
    d.quit()
  }

  private def deleteProfileFolder(){
    log.info(s"Deleting profile directory for $sessionID")
    org.apache.commons.io.FileUtils.deleteDirectory(new java.io.File("/tmp/ff-downloads/%s".format(sessionID)))
  }

  private def wrap(base: Receive) = new Receive {
    def isDefinedAt(arg: Any) = base.isDefinedAt(arg)
    def apply(arg: Any) = {
      val clazz = arg.getClass.toString
      val reqID = java.util.UUID.randomUUID()
      try {
        log.info("####==> Received request: SessionID=[%s]; reqID=[%s]; for=[%s] from %s.".format(sessionID, reqID, arg.toString, sender.path.toString))
        base.apply(arg)
      } catch {
        case e: Exception =>
          if (sender == ActorFactory.system.deadLetters) {
            log.error(s"Error[DeadLetters] occured while processing [$clazz]", e)
          } else {
            log.error(s"Error occured while processing [$clazz]", e)
          }

          sender ! new Exception(e.stackTrace)
      } finally {
        log.info("####==> Sending response: SessionID=[%s]; reqID=[%s]; for=[%s] from %s.".format(sessionID, reqID, arg.toString, sender.path.toString))
      }
    }
  }

  def receive = wrap(receiveBase)

  private def schedulify(data: KeepaliveData) {
    context.system.scheduler.scheduleOnce(data.delay, self, data)
  }

  private def startKeepalive(data: KeepaliveData) {
    stopKeepalive
//    log.info("Starting keepalive.")
//    log.debug(keepaliveStatus)
    keepaliveScheduler = Some(context.system.scheduler.schedule(Duration.Zero, data.delay, self, data))
  }

  private def stopKeepalive() {
//    log.info("Stopping keepalive.")
//    log.debug(keepaliveStatus)
    keepaliveScheduler.map(_.cancel)
    keepaliveScheduler = None
  }

  private def keepaliveStatus =
    "Previous status: %s.".format(if (isKeepalive) "running" else "stopped")
}
