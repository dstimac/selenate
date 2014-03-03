package net.selenate
package server
package driver

import org.openqa.selenium.firefox.FirefoxProfile

import scala.collection.immutable.TreeMap

object DriverProfile {
  private object IsString {
    private val R = """'(.*)'"""r
    def unapply(raw: String): Option[java.lang.String] =
      tryo {
        val R(extracted) = raw
        extracted
      }
  }

  private object IsInt {
    def unapply(raw: String): Option[java.lang.Integer] =
      tryo {
        raw.toInt
      }
  }

  private object IsBoolean {
    def unapply(raw: String): Option[java.lang.Boolean] =
      tryo {
        raw.toBoolean
      }
  }

  def empty = new DriverProfile(Map.empty)

  def fromString(s: String) = parseProfile(s)

  private def addPref(ffp: FirefoxProfile)(pref: (String, AnyRef)) {
    val k = pref._1
    val v = pref._2

    v match {
      case x: java.lang.Boolean => ffp.setPreference(k, x)
      case x: java.lang.Integer => ffp.setPreference(k, x)
      case x: java.lang.String  => ffp.setPreference(k, x)
      case x => throw new IllegalArgumentException("Unsupported type: %s" format x.getClass.getCanonicalName)
    }
  }

  private def getFFP(prefMap: Map[String, AnyRef]) = {
    val ffp = new FirefoxProfile

    // Allow access to frames
//    ffp.setPreference("capability.policy.default.Window.QueryInterface", "allAccess");
//    ffp.setPreference("capability.policy.default.Window.frameElement.get","allAccess");
    prefMap foreach addPref(ffp)
    ffp
  }

  private def parseProfile(s: String) = {
    val entryList = s.split(";")
    new DriverProfile(entryList flatMap parseEntry toMap)
  }

  private def parseEntry(entry: String): Option[(String, AnyRef)] =
    entry.split("=").toList match {
      case key :: IsBoolean(value) :: Nil => Some(key -> value)
      case key :: IsInt(value)     :: Nil => Some(key -> value)
      case key :: IsString(value)  :: Nil => Some(key -> value)
      case "" :: Nil => None
      case _ => throw new Exception("Error while parsing profile config. Offending entry:\n%s" format entry)
    }

  private def serializeProfile(p: DriverProfile): String = {
    def key(e: (String, AnyRef)) = e._1
    p.prefMap.toSeq sortBy key map serializeEntry mkString ";"
  }

  private def serializeEntry(entry: (String, AnyRef)) =
    "%s=%s".format(entry._1, entry._2)
}


class DriverProfile(val prefMap: Map[String, AnyRef]) {
  import DriverProfile._

  override def toString = "DriverProfile(%s)" format signature

  /*private[driver]*/ val get = getFFP(prefMap)
  /*private[driver]*/ val signature = serializeProfile(this)
}
