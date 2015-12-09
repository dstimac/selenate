package net.selenate.server

import org.streum.configrity.Configuration

object C {
  val branch             = sys.props("branch")
  private val configPath = sys.props("user.home") / ".config" / "selenate" / branch / "server.config"
  private val config     = Configuration.load(configPath)

  object Server {
    private val serverConfig = config.detach("server")
    val poolSize          = serverConfig[Int]("pool-size")
    val host              = serverConfig[String]("host")
    val defaultProfileOpt = serverConfig.get[String]("default-profile")

    val gridConfig = config.detach("selenium-grid")
    val gridURL = gridConfig.get[String]("gridUrl")
    val gridPercentage = gridConfig.get[Int]("gridPercentage")
  }
}
