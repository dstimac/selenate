package net.selenate.server
package extensions

import info.ProfileInfo

import java.io.File
import org.openqa.selenium.firefox.FirefoxBinary

object SelenateBinary {
  def fromProfileInfo(profile: ProfileInfo) =
    new SelenateBinary(profile.binaryLocation)

  def DefaultBinaryLocation = new SelenateBinary().binaryLocation
}

class SelenateBinary(binaryLocationOpt: Option[File]) extends FirefoxBinary(binaryLocationOpt.orNull) {
  def this(binaryLocation: File) = this(Some(binaryLocation))
  def this() = this(None)

  val binaryLocation = getExecutable.getFile
  override def toString = s"SelenateBinary(${ getExecutable.getPath })"
}
