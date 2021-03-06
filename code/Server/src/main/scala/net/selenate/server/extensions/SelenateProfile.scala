package net.selenate.server
package extensions

import settings.ProfileSettings

import java.io.File
import net.selenate.common.exceptions.SeException
import org.openqa.selenium.firefox.FirefoxProfile

object SelenateProfile {
  def fromProfileSettings(profile: ProfileSettings) =
    new SelenateProfile(profile.prefMap, None)
}

class SelenateProfile(prefMap: Map[String, AnyRef], profileDirOpt: Option[File]) extends FirefoxProfile(profileDirOpt.orNull) {
  def this(prefMap: Map[String, AnyRef], profileDir: File) = this(prefMap, Some(profileDir))
  def this(prefMap: Map[String, AnyRef]) = this(prefMap, None)

  private def addPref(pref: (String, AnyRef)) {
    val k = pref._1
    val v = pref._2

    v match {
      case x: java.lang.Boolean => this.setPreference(k, x)
      case x: java.lang.Integer => this.setPreference(k, x)
      case x: java.lang.String  => this.setPreference(k, x)
      case x => throw new SeException("Unsupported type: %s" format x.getClass.getCanonicalName)
    }
  }

  prefMap foreach addPref
}
