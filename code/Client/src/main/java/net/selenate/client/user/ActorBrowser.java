package net.selenate.client.user;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import net.selenate.client.interfaces.IBrowser;
import net.selenate.client.interfaces.IElement;
import net.selenate.client.interfaces.INavigation;
import net.selenate.common.comms.SeDownloadMethod;
import net.selenate.common.comms.SeElementSelectMethod;
import net.selenate.common.comms.SeElementSelector;
import net.selenate.common.comms.req.SeCommsReq;
import net.selenate.common.comms.req.SeReqAddCookie;
import net.selenate.common.comms.req.SeReqCapture;
import net.selenate.common.comms.req.SeReqCaptureWindow;
import net.selenate.common.comms.req.SeReqDeleteCookieNamed;
import net.selenate.common.comms.req.SeReqDownload;
import net.selenate.common.comms.req.SeReqElementExists;
import net.selenate.common.comms.req.SeReqExecuteScript;
import net.selenate.common.comms.req.SeReqFindAlert;
import net.selenate.common.comms.req.SeReqFindAndClick;
import net.selenate.common.comms.req.SeReqFindElement;
import net.selenate.common.comms.req.SeReqFindElementList;
import net.selenate.common.comms.req.SeReqGet;
import net.selenate.common.comms.req.SeReqNavigateBack;
import net.selenate.common.comms.req.SeReqNavigateForward;
import net.selenate.common.comms.req.SeReqNavigateRefresh;
import net.selenate.common.comms.req.SeReqQuit;
import net.selenate.common.comms.req.SeReqResetFrame;
import net.selenate.common.comms.req.SeReqSetUseFrames;
import net.selenate.common.comms.req.SeReqSikuliClick;
import net.selenate.common.comms.req.SeReqSikuliImageExists;
import net.selenate.common.comms.req.SeReqSikuliInputText;
import net.selenate.common.comms.req.SeReqSikuliTakeScreenshot;
import net.selenate.common.comms.req.SeReqStartKeepalive;
import net.selenate.common.comms.req.SeReqStopKeepalive;
import net.selenate.common.comms.req.SeReqSwitchFrame;
import net.selenate.common.comms.req.SeReqWaitForBrowserPage;
import net.selenate.common.comms.res.SeResAddCookie;
import net.selenate.common.comms.res.SeResCapture;
import net.selenate.common.comms.res.SeResCaptureWindow;
import net.selenate.common.comms.res.SeResDeleteCookieNamed;
import net.selenate.common.comms.res.SeResDownload;
import net.selenate.common.comms.res.SeResElementExists;
import net.selenate.common.comms.res.SeResExecuteScript;
import net.selenate.common.comms.res.SeResFindAlert;
import net.selenate.common.comms.res.SeResFindElement;
import net.selenate.common.comms.res.SeResFindElementList;
import net.selenate.common.comms.res.SeResGet;
import net.selenate.common.comms.res.SeResNavigateBack;
import net.selenate.common.comms.res.SeResNavigateForward;
import net.selenate.common.comms.res.SeResNavigateRefresh;
import net.selenate.common.comms.res.SeResQuit;
import net.selenate.common.comms.res.SeResResetFrame;
import net.selenate.common.comms.res.SeResSetUseFrames;
import net.selenate.common.comms.res.SeResSikuliClick;
import net.selenate.common.comms.res.SeResSikuliImageExists;
import net.selenate.common.comms.res.SeResSikuliInputText;
import net.selenate.common.comms.res.SeResSikuliTakeScreenshot;
import net.selenate.common.comms.res.SeResStartKeepalive;
import net.selenate.common.comms.res.SeResStopKeepalive;
import net.selenate.common.comms.res.SeResSwitchFrame;
import net.selenate.common.comms.res.SeResWaitForBrowserPage;
import net.selenate.common.user.BrowserPage;
import net.selenate.common.user.Capture;
import net.selenate.common.user.Cookie;
import net.selenate.common.user.ElementSelectMethod;
import net.selenate.common.user.ElementSelector;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ActorBrowser extends ActorBase implements IBrowser {
  private final ActorSystem system;

  public ActorBrowser(final ActorRef session, final ActorSystem system) {
    super(session);
    this.system = system;
  }

  @Override
  public void open(final String url) throws IOException {
    typedBlock(new SeReqGet(url), SeResGet.class);
  }

  @Override
  public Capture capture(final String name) throws IOException {
    final SeResCapture res = typedBlock(new SeReqCapture(name), SeResCapture.class);
    return resToUserCapture(res);
  }

  @Override
  public Capture capture(final String name, final boolean takeScreenshot) throws IOException {
    final SeResCapture res = typedBlock(new SeReqCapture(name, takeScreenshot), SeResCapture.class);
    return resToUserCapture(res);
  }

  @Override
  public byte[] captureWindow(final ElementSelector selector, final String cssElement) throws IOException {
    final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(selector.method);
    final SeResCaptureWindow res = typedBlock(
      new SeReqCaptureWindow(reqMethod, selector.query, cssElement),
      SeResCaptureWindow.class);
    return res.screenshot;
  }

  @Override
  public String executeScript(final String javascript) throws IOException {
    final SeResExecuteScript res = typedBlock(new SeReqExecuteScript(javascript), SeResExecuteScript.class);
    return res.result;
  }

  @Override
  public void resetFrame() throws IOException {
    typedBlock(new SeReqResetFrame(), SeResResetFrame.class);
  }

  @Override
  public void switchFrame(final int frame) throws IOException {
    typedBlock(new SeReqSwitchFrame(frame), SeResSwitchFrame.class);
  }

  @Override
  public void switchFrame(final ElementSelectMethod method, final String query) throws IOException {
    switchFrame(new ElementSelector(method, query));
  }

  @Override
  public void switchFrame(final ElementSelector selector) throws IOException {
    final SeElementSelector reqSelector = userToReqSelector(selector);
    typedBlock(new SeReqSwitchFrame(reqSelector), SeResSwitchFrame.class);
  }

  @Override
  public boolean waitFor(final List<ElementSelector> selectorList) throws IOException {
    return waitFor(new BrowserPage("default", selectorList));
  }

  @Override
  public boolean waitFor(final ElementSelector... selectorList) throws IOException {
    return waitFor(new BrowserPage("default", new ArrayList<ElementSelector>(Arrays.asList(selectorList))));
  }

  @Override
  public boolean waitFor(final BrowserPage page) throws IOException {
    final List<BrowserPage> pageList = new ArrayList<BrowserPage>();
    pageList.add(page);
    final String res = waitForAny(pageList);
    return (res != null);
  }

  @Override
  public String waitForAny(final BrowserPage ... pageList) throws IOException {
    final BrowserPage resPage = waitForAnyPage(Arrays.asList(pageList));
    if(resPage == null) return null;
    return resPage.name;
  }

  @Override
  public String waitForAny(final List<BrowserPage> pageList) throws IOException {
    final BrowserPage resPage = waitForAnyPage(pageList);
    if(resPage == null) return null;
    return resPage.name;
  }

  @Override
  public void quit() throws IOException {
    typedBlock(new SeReqQuit(), SeResQuit.class); // kill browser.
    try {
      final Future<Boolean> stopped = akka.pattern.Patterns.gracefulStop(session, Duration.create(5, TimeUnit.SECONDS), system);
      Await.result(stopped, Duration.create(6, TimeUnit.SECONDS));
    } catch (final Exception e) {
      throw new IOException("Error while trying to gracefully stop the actor session.", e);
    }
  }

  @Override
  public boolean elementExists(
      final ElementSelectMethod method,
      final String query)
          throws IOException {
    final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
    final SeResElementExists res = typedBlock(new SeReqElementExists(reqMethod, query), SeResElementExists.class);

    return res.isFound;
  }

  @Override
  public boolean elementExists(final ElementSelector selector) throws IOException {
    return elementExists(selector.method, selector.query);
  }

  @Override
  public IElement findElement(
      final ElementSelectMethod method,
      final String query)
      throws IOException {
    final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
    final SeResFindElement res = typedBlock(new SeReqFindElement(reqMethod, query), SeResFindElement.class);

    return resToUserElement(res.element);
  }

  @Override
  public IElement findElement(final ElementSelector selector) throws IOException {
    return findElement(selector.method, selector.query);
  }

  @Override
  public List<IElement> findElementList(
      final ElementSelectMethod method,
      final String query)
          throws IOException {
    final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
    final SeResFindElementList res = typedBlock(new SeReqFindElementList(reqMethod, query), SeResFindElementList.class);

    return resToUserElementList(res.elementList);
  }

  @Override
  public List<IElement> findElementList(final ElementSelector selector) throws IOException {
    return findElementList(selector.method, selector.query);
  }

  @Override
  public String findAlert() throws IOException {
    final SeResFindAlert res = typedBlock(new SeReqFindAlert(), SeResFindAlert.class);
    return res.text;
  }

  @Override
  public void deleteCookieNamed(final String name) throws IOException {
    typedBlock(new SeReqDeleteCookieNamed(name), SeResDeleteCookieNamed.class);
  }

  @Override
  public void addCookie(final Cookie cookie) throws IOException {
    typedBlock(new SeReqAddCookie(cookie), SeResAddCookie.class);
  }

  @Override
  public INavigation navigate() throws IOException {
    return new ActorNavigation();
  }

  @Override
  public byte[] download(final String url, final SeDownloadMethod method, final Map<String, String> headers, final byte[] body) throws IOException {
    final SeResDownload res = typedBlock(new SeReqDownload(url, method, headers, body), SeResDownload.class);
    return res.body;
  }

  @Override
  public void startKeepaliveClick(final long delayMillis, final ElementSelector selector) throws IOException {
    startKeepaliveClick(delayMillis, selector.method, selector.query);
  }

  @Override
  public void startKeepaliveClick(final long delayMillis, final ElementSelectMethod method, final String query) throws IOException {
    final SeElementSelectMethod reqMethod = userToReqElementSelectMethod(method);
    final List<SeCommsReq> reqList = new ArrayList<SeCommsReq>();
    reqList.add(new SeReqFindAndClick(reqMethod, query));

    startKeepalive(delayMillis, reqList);
  }

  @Override
  public void startKeepalive(final long delayMillis, final SeCommsReq... reqList) throws IOException {
    startKeepalive(delayMillis, Arrays.asList(reqList));
  }

  @Override
  public void startKeepalive(final long delayMillis, final List<SeCommsReq> reqList) throws IOException {
    typedBlock(new SeReqStartKeepalive(delayMillis, reqList), SeResStartKeepalive.class);
  }

  @Override
  public void stopKeepalive() throws IOException {
    typedBlock(new SeReqStopKeepalive(), SeResStopKeepalive.class);
  }


  private class ActorNavigation implements INavigation {
    @Override
    public void back() throws IOException {
      typedBlock(new SeReqNavigateBack(), SeResNavigateBack.class);
    }

    @Override
    public void forward() throws IOException {
      typedBlock(new SeReqNavigateForward(), SeResNavigateForward.class);
    }

    @Override
    public void refresh() throws IOException {
      typedBlock(new SeReqNavigateRefresh(), SeResNavigateRefresh.class);
    }
  }

  @Override
  public BrowserPage waitForAnyPage(final BrowserPage... pageList) throws IOException {
    return waitForAnyPage(Arrays.asList(pageList));
  }

  @Override
  public BrowserPage waitForAnyPage(final List<BrowserPage> pageList) throws IOException {
    final SeResWaitForBrowserPage res = typedBlock(new SeReqWaitForBrowserPage(userToReqPageList(pageList)), SeResWaitForBrowserPage.class);

    return res.isSuccessful ? res.foundPage : null;
  }

  @Override
  public void setAutoFrames(final Boolean useFrames) throws IOException {
    typedBlock(new SeReqSetUseFrames(useFrames), SeResSetUseFrames.class);
  }

  @Override
  public boolean sikuliImageExists(final byte[] image) throws IOException {
    final SeResSikuliImageExists res = typedBlock(new SeReqSikuliImageExists(image, 5000), SeResSikuliImageExists.class);
    return res.isImageFound();
  }
  @Override
  public void sikuliClick(final byte[] image) throws IOException {
    typedBlock(new SeReqSikuliClick(image, 5000), SeResSikuliClick.class);
  }
  @Override
  public byte[] sikuliTakeScreenshot(final byte[] image, final int width, final int height) throws IOException {
    final SeResSikuliTakeScreenshot res = typedBlock(new SeReqSikuliTakeScreenshot(image, width, height), SeResSikuliTakeScreenshot.class);
    return res.getImage();
  }
  @Override
  public void sikuliInputText(final byte[] image, final String text) throws IOException {
    typedBlock(new SeReqSikuliInputText(image, text), SeResSikuliInputText.class);
  }

  @Override
  public void setTimeout(final int seconds){
    this.timeout = new Timeout(seconds, TimeUnit.SECONDS);
  }
}
