package net.selenate.server
package sessions

import driver.DriverPoolActor

import akka.actor.{ Actor, ActorRef, Props }
import net.selenate.common.sessions.SessionRequest

object SessionFactoryActor {
  def props() = Props(new SessionFactoryActor())
}

class SessionFactoryActor
    extends Actor
    with Loggable {
  import SessionFactoryActor._

  def receive = {
    case req: SessionRequest =>
      logDebug(s"""Received SessionRequest(${ req.getSessionID })""")
      context.actorSelection("akka://server-system/user/driver-pool") ! DriverPoolActor.Dequeue(sender, req)

    case (requester: ActorRef, sessionRequest: SessionRequest, DriverPoolActor.DriverEntry(uuid, driverFuture)) =>
      val sessionID = sessionRequest.getSessionID

      logDebug(s"""Received DriverEntry($uuid) for session "$sessionID"""")
      driverFuture.onSuccess {
        case driver =>
          logDebug(s"""Driver future for session "$sessionID" completed""")
          requester ! context.actorOf(SessionActor.props(sessionRequest, driver), sessionID)
      }
  }

  override def postStop() {
    logDebug("Session factory stopped")
  }
}
