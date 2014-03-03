package net.selenate
package server
package sessions
package actions

import common.comms._
import req._
import res._
import java.util.ArrayList
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.{ RemoteWebDriver, RemoteWebElement, UselessFileDetector }
import org.openqa.selenium.SearchContext
import org.openqa.selenium.{ By, WebElement }
import org.openqa.selenium.support.ui.Select
import scala.collection.JavaConversions._
import net.selenate.common.util.Util
import net.selenate.common.user.BrowserPage
import net.selenate.common.user.ElementSelector
import net.selenate.common.user.ElementSelectMethod

trait ActionCommons {
  type Window      = String
  type Frame       = Int
  type FrameBy     = By
  type FramePath   = IndexedSeq[Frame]
  type FramePathBy = IndexedSeq[FrameBy]

  case class Address(window: Window, framePath: FramePath)
  case class AddressBy(window: Window, framePath: FramePathBy)


  val d: FirefoxDriver
  protected val log: net.selenate.server.Log[_]

  object JS {
    val getAttributes = """
var report = [];
var attrList = arguments[0].attributes;
for (var n = 0; n < attrList.length; n++) {
  var entry = attrList[n];
  report.push(entry.name + ' -> ' + entry.value);
};
return report;
"""
  }


  protected object SelenateWebElement {
    def apply(parent: RemoteWebDriver, uuid: String) = {
      val e = new SelenateWebElement
      e.setParent(parent);
      e.setId(uuid);
      e.setFoundBy(parent, "UUID", uuid)
      e.setFileDetector(new UselessFileDetector())
      e
    }
  }
  protected class SelenateWebElement private () extends RemoteWebElement {
    override def setFoundBy(parent: SearchContext, locator: String, term: String) {
      super.setFoundBy(parent, locator, term);
    }
  }

  protected def switchToWindow(window: Window) {
    log.debug("SWITCHING TO WINDOW "+ window)
    d.switchTo.window(window)
    log.debug("SWITCHING TO DEFAULT CONTENT")
    d.switchTo.defaultContent
  }

  protected def switchToFrame(window: Window, framePath: FramePath) {
    switchToWindow(window)
    framePath foreach { e =>
      log.debug("SWITCHING TO FRAME "+ e)
      d.switchTo.frame(e)
    }
  }

  protected def switchToFrameBy(window: Window, framePath: FramePathBy) {
    switchToWindow(window)
    framePath foreach { e =>
      log.debug("SWITCHING TO FRAME "+ "(%s)".format(e.toString()))
      val elem = d.findElement(e)
      d.switchTo.frame(elem)
    }
  }

  protected def findElementList(method: SeElementSelectMethod, query: String) = {
    import SeElementSelectMethod._
    type Selector = String => IndexedSeq[RemoteWebElement]

    def findByBy(byFactory: (String) => By): Selector = { query =>
      d.findElements(byFactory(query)).map(_.asInstanceOf[RemoteWebElement]).toIndexedSeq
    }

    def findByUUID(): Selector = { query =>
      IndexedSeq(SelenateWebElement(d, query))
    }

    val elementFactory = method match {
      case CLASS_NAME        => findByBy(By.className _)
      case CSS_SELECTOR      => findByBy(By.cssSelector _)
      case ID                => findByBy(By.id _)
      case LINK_TEXT         => findByBy(By.linkText _)
      case NAME              => findByBy(By.name _)
      case PARTIAL_LINK_TEXT => findByBy(By.partialLinkText _)
      case TAG_NAME          => findByBy(By.tagName _)
      case UUID              => findByUUID()
      case XPATH             => findByBy(By.xpath _)
    }

    elementFactory(query)
  }

  protected def findElement(method: SeElementSelectMethod, query: String) = {
    import SeElementSelectMethod._
    type Selector = String => RemoteWebElement

    def findByBy(byFactory: (String) => By): Selector = { query =>
      d.findElement(byFactory(query)).asInstanceOf[RemoteWebElement]
    }

    def findByUUID(): Selector = { query =>
      SelenateWebElement(d, query)
    }

    val elementFactory = method match {
      case CLASS_NAME        => findByBy(By.className _)
      case CSS_SELECTOR      => findByBy(By.cssSelector _)
      case ID                => findByBy(By.id _)
      case LINK_TEXT         => findByBy(By.linkText _)
      case NAME              => findByBy(By.name _)
      case PARTIAL_LINK_TEXT => findByBy(By.partialLinkText _)
      case TAG_NAME          => findByBy(By.tagName _)
      case UUID              => findByUUID()
      case XPATH             => findByBy(By.xpath _)
    }

    elementFactory(query)
  }

  protected def selectOption(s: Select, method: SeOptionSelectMethod, query: String) = {
    import SeOptionSelectMethod._
    def selectorFactory = method match {
      case INDEX        => (str: String) => s.selectByIndex(str.toInt)
      case VALUE        => s.selectByValue _
      case VISIBLE_TEXT => s.selectByVisibleText _
    }

    selectorFactory(query)
  }

  protected def parseWebElement(address: Address)(e: RemoteWebElement): SeElement = {
    val attributeReport = d.executeScript(JS.getAttributes, e)
    new SeElement(
        e.getId,
        e.getLocation.getX,
        e.getLocation.getY,
        e.getSize.getWidth,
        e.getSize.getHeight,
        e.getTagName,
        e.getText,
        e.isDisplayed,
        e.isEnabled,
        e.isSelected,
        address.window,
        seqToRealJava(address.framePath map toInteger),
        mapToRealJava(parseAttributeReport(attributeReport)))
  }

  protected def parseWebElement(address: AddressBy)(e: RemoteWebElement): SeElement = {
    val attributeReport = d.executeScript(JS.getAttributes, e)
    new SeElement(
        e.getId,
        e.getLocation.getX,
        e.getLocation.getY,
        e.getSize.getWidth,
        e.getSize.getHeight,
        e.getTagName,
        e.getText,
        e.isDisplayed,
        e.isEnabled,
        e.isSelected,
        address.window,
        seqToRealJava(List.empty),
        mapToRealJava(parseAttributeReport(attributeReport)))
  }

  protected def parseSelectElement(address: Address)(e: RemoteWebElement): SeSelect = {
    val select = new Select(e)
    val rawAllOptionList         = select.getOptions.map(_.asInstanceOf[RemoteWebElement]).toIndexedSeq
    val rawSelectedOptionList    = select.getAllSelectedOptions.map(_.asInstanceOf[RemoteWebElement]).toIndexedSeq
    val selectedIndexList        = rawAllOptionList.zipWithIndex.collect { case(o, i) if rawSelectedOptionList.contains(o) => i }

    val parsedAllOptionList      = rawAllOptionList map parseOptionElement(address)
    val parsedSelectedOptionList = rawSelectedOptionList map parseOptionElement(address)

    val firstSelectedIndex       = selectedIndexList.headOption
    val firstSelectedOption      = parsedSelectedOptionList.headOption

    new SeSelect(
        parseWebElement(address)(e),
        parsedAllOptionList.size,
        firstSelectedIndex map toInteger orNull,
        firstSelectedOption.orNull,
        seqToRealJava(parsedAllOptionList))
  }

  protected def parseSelectElement(address: AddressBy)(e: RemoteWebElement): SeSelect = {
    val select = new Select(e)
    val rawAllOptionList         = select.getOptions.map(_.asInstanceOf[RemoteWebElement]).toIndexedSeq
    val rawSelectedOptionList    = select.getAllSelectedOptions.map(_.asInstanceOf[RemoteWebElement]).toIndexedSeq
    val selectedIndexList        = rawAllOptionList.zipWithIndex.collect { case(o, i) if rawSelectedOptionList.contains(o) => i }

    val parsedAllOptionList      = rawAllOptionList map parseOptionElement(address)
    val parsedSelectedOptionList = rawSelectedOptionList map parseOptionElement(address)

    val firstSelectedIndex       = selectedIndexList.headOption
    val firstSelectedOption      = parsedSelectedOptionList.headOption

    new SeSelect(
        parseWebElement(address)(e),
        parsedAllOptionList.size,
        firstSelectedIndex map toInteger orNull,
        firstSelectedOption.orNull,
        seqToRealJava(parsedAllOptionList))
  }

  protected def parseOptionElement(address: Address)(e: RemoteWebElement): SeOption = {
    new SeOption(parseWebElement(address)(e))
  }
  protected def parseOptionElement(address: AddressBy)(e: RemoteWebElement): SeOption = {
    new SeOption(parseWebElement(address)(e))
  }

  protected def parseAttributeReport(reportRaw: Object): Map[String, String] =
    reportRaw match {
      case report: ArrayList[_] =>
        val attributeList: List[(String, String)] =
          report.toList flatMap {
            case entry: String =>
              val elements = entry.split(" -> ").toList
              elements match {
                case attribute :: value :: Nil =>
                  Some(attribute -> value)
                case _ =>
                  None
              }
            case _ =>
              None
          }
        attributeList.toMap
      case _ =>
        Map.empty
    }


  protected def inAllWindows[T](address: Address => T): Stream[T] = {
    val windowList: Stream[Window] = d.getWindowHandles().toStream
    windowList flatMap inAllFrames(address)
  }

  protected def inAllWindowsByBy[T](address: AddressBy => T): Stream[T] = {
    val windowList: Stream[Window] = d.getWindowHandles().toStream
    windowList flatMap inAllFramesByBy(address)
  }


  protected def inAllFrames[T](address: Address => T)(window: Window): Stream[T] = {
    def findAllFrames: FramePath = {
      val raw = d.findElementsByXPath("//*[local-name()='frame' or local-name()='iframe']").toIndexedSeq.zipWithIndex
      raw map { case (elem, index) => index }
    }

    def inAllFramesDoit(window: Window, framePath: Vector[Frame], frame: Option[Frame]): Stream[T] = {
      val fullPath = framePath ++ frame.toIndexedSeq
      switchToFrame(window, fullPath)
      val result = address(Address(window, fullPath))
      log.debug("###############==========-----> [%s]: %s".format(fullPath.mkString(", "), result))
      val childrenResultList = findAllFrames.toStream flatMap { f =>
        inAllFramesDoit(window, fullPath, Some(f))
      }

      childrenResultList :+ result
    }

    inAllFramesDoit(window, Vector(), None)
  }


  protected def getUniqueElementXpath(element: WebElement): String = {
    val attributeReport = d.executeScript(JS.getAttributes, element)
    val attributes      = parseAttributeReport(attributeReport)

    val classes = attributes.get("class")
    val id      = attributes.get("id")
    val name    = attributes.get("name")
    val src     = attributes.get("src")

    val attrXpath = (src.map("@src='%s'".format(_)) ++ classes.map("@class='%s'".format(_)) ++ id.map("@id='%s'".format(_)) ++ name.map("@name='%s'".format(_))).toList
    Util.listToString(attrXpath, "//*[", " or ", "]")
  }

  protected def inAllFramesByBy[T](address: AddressBy => T)(window: Window): Stream[T] = {
    def findAllFrames: FramePathBy = {

      val raw = findElementList(SeElementSelectMethod.XPATH, "//*[local-name()='frame' or local-name()='iframe']").toIndexedSeq.zipWithIndex
//      val raw = d.findElementsByXPath("//*[local-name()='frame' or local-name()='iframe']").toIndexedSeq.zipWithIndex

      raw map { case (elem, index) => By.xpath(getUniqueElementXpath(elem)) }
    }

    def inAllFramesDoit(window: Window, framePath: Vector[FrameBy], frame: Option[FrameBy]): Stream[T] = {
      val fullPath = framePath ++ frame.toIndexedSeq
      switchToFrameBy(window, fullPath)
      val result = address(AddressBy(window, fullPath))
      log.debug("###############==========-----> [%s]: %s".format(fullPath.mkString(", "), result))
      val childrenResultList = findAllFrames.toStream flatMap { f =>
        inAllFramesDoit(window, fullPath, Some(f))
      }

      childrenResultList :+ result
    }

    inAllFramesDoit(window, Vector(), None)
  }

  protected def toBrowserPage(page: SePage): BrowserPage = {
    new BrowserPage(page.name, toElementSelectorList(page.selectorList))
  }

  protected def toElementSelectorList(seElementList: java.util.List[SeElementSelector]): java.util.List[ElementSelector] = {
    seElementList.map(e =>
      new ElementSelector(reqSelectMethodToUserSelectMethod(e.method), e.query)
      )
  }

  import SeElementSelectMethod._
  protected def reqSelectMethodToUserSelectMethod(method: SeElementSelectMethod) =  method match {
      case CLASS_NAME        => ElementSelectMethod.CLASS_NAME
      case CSS_SELECTOR      => ElementSelectMethod.CSS_SELECTOR
      case ID                => ElementSelectMethod.ID
      case LINK_TEXT         => ElementSelectMethod.LINK_TEXT
      case NAME              => ElementSelectMethod.NAME
      case PARTIAL_LINK_TEXT => ElementSelectMethod.PARTIAL_LINK_TEXT
      case TAG_NAME          => ElementSelectMethod.TAG_NAME
      case UUID              => ElementSelectMethod.UUID
      case XPATH             => ElementSelectMethod.XPATH
  }
}
