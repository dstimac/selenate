package net.selenate.client.interfaces;

import net.selenate.common.comms.SeDownloadMethod;
import net.selenate.common.comms.req.SeCommsReq;
import net.selenate.common.user.BrowserPage;
import net.selenate.common.user.Capture;
import net.selenate.common.user.Cookie;
import net.selenate.common.user.ElementSelectMethod;
import net.selenate.common.user.ElementSelector;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IBrowser {
  public void open(String url) throws IOException;
  public Capture capture(String name) throws IOException;
  public Capture capture(String name, boolean takeScreenshot) throws IOException;
  public byte[] captureWindow(ElementSelector selector, String cssElement) throws IOException;
  public String executeScript(String javascript) throws IOException;
  public void resetFrame() throws IOException;
  public void switchFrame(int frame) throws IOException;
  public void switchFrame(final ElementSelectMethod method, final String query) throws IOException;
  public void switchFrame(final ElementSelector selector) throws IOException;
  public void setAutoFrames(final Boolean useFrames) throws IOException;

  public boolean waitFor(List<ElementSelector> selectorList) throws IOException;
  public boolean waitFor(ElementSelector... selectorList) throws IOException;
  public boolean waitFor(BrowserPage page) throws IOException;
  public String waitForAny(BrowserPage... pageList) throws IOException;
  public String waitForAny(List<BrowserPage> pageList) throws IOException;
  public BrowserPage waitForAnyPage(BrowserPage... pageList) throws IOException;
  public BrowserPage waitForAnyPage(List<BrowserPage> pageList) throws IOException;

  public void quit() throws IOException;

  public boolean elementExists(ElementSelectMethod method, String query) throws IOException;
  public boolean elementExists(ElementSelector selector) throws IOException;

  public IElement findElement(ElementSelectMethod method, String query) throws IOException;
  public IElement findElement(ElementSelector selector) throws IOException;

  public List<IElement> findElementList(ElementSelectMethod method, String query) throws IOException;
  public List<IElement> findElementList(ElementSelector selector) throws IOException;

  public String findAlert() throws IOException;

  public void deleteCookieNamed(String name) throws IOException;
  public void addCookie(Cookie cookie) throws IOException;

  public INavigation navigate() throws IOException;

  public byte[] download(String url, SeDownloadMethod method, Map<String, String> headers, byte[] body) throws IOException;

  public void startKeepaliveClick(long delayMillis, ElementSelectMethod method, String query) throws IOException;
  public void startKeepaliveClick(long delayMillis, ElementSelector selector) throws IOException;

  public void startKeepalive(long delayMillis, SeCommsReq... reqList) throws IOException;
  public void startKeepalive(long delayMillis, List<SeCommsReq> reqList) throws IOException;

  public void stopKeepalive() throws IOException;

  public boolean sikuliImageExists(final byte[] image) throws IOException;
  public void sikuliClick(final byte[] image) throws IOException;
  public byte[] sikuliTakeScreenshot(final byte[] image, final int width, final int height) throws IOException;
  public void sikuliInputText(final byte[] image, final String text) throws IOException;

  public void setTimeout(int seconds);
}