package net.selenate
package server
package driver

import actors.ActorFactory

object DriverPool extends DriverFactory {
  val size           = C.Server.poolSize
  val defaultProfile = C.Server.defaultProfileOpt map DriverProfile.fromString getOrElse DriverProfile.empty
  val defaultPool = ActorFactory.typed[IDriverPoolActor]("driver-pool", new DriverPoolActor(defaultProfile, size))

  def get(profile: DriverProfile) = {
    if (profile.signature == defaultProfile.signature) {
      defaultPool.get
    } else {
      val options = DriverOptions(Map.empty, C.Server.gridURL, C.Server.gridPercentage)
      createDriver(options)
    }
  }
}
