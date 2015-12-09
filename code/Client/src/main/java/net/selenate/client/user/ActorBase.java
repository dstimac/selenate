package net.selenate.client.user;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import net.selenate.client.interfaces.IElement;
import net.selenate.client.interfaces.IOption;
import net.selenate.common.comms.SeCookie;
import net.selenate.common.comms.SeElement;
import net.selenate.common.comms.SeElementSelectMethod;
import net.selenate.common.comms.SeElementSelector;
import net.selenate.common.comms.SeFrame;
import net.selenate.common.comms.SeOption;
import net.selenate.common.comms.SeOptionSelectMethod;
import net.selenate.common.comms.SePage;
import net.selenate.common.comms.SeSelect;
import net.selenate.common.comms.SeWindow;
import net.selenate.common.comms.res.SeResCapture;
import net.selenate.common.user.BrowserPage;
import net.selenate.common.user.Capture;
import net.selenate.common.user.CaptureCookie;
import net.selenate.common.user.CaptureFrame;
import net.selenate.common.user.CaptureWindow;
import net.selenate.common.user.ElementSelectMethod;
import net.selenate.common.user.ElementSelector;
import net.selenate.common.user.Location;
import net.selenate.common.user.OptionSelectMethod;
import net.selenate.common.user.Position;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class ActorBase {
  protected Timeout timeout = new Timeout(30, TimeUnit.SECONDS);
  protected final ActorRef session;

  public ActorBase(final ActorRef session) {
    if (session == null) {
      throw new IllegalArgumentException("Session cannot be null!");
    }

    this.session = session;
  }

  protected Object block(final Object req) throws IOException {
    try {
      final Future<Object> future = Patterns.ask(session, req, timeout);
      final Object result = Await.result(future, new Timeout(32, TimeUnit.SECONDS).duration());
      return result;
    } catch (final Exception e) {
      throw new IOException(String.format("An error occured while sending the message to remote actor!\nMessage: %s", req.toString()), e);
    }
  }

  protected <T> T typedBlock(
      final Object req,
      final Class<T> clazz) throws IOException {
    final Object obj = block(req);
    try {
      final T res = clazz.cast(obj);
      return res;
    } catch (final ClassCastException e0) {
      try {
        final Exception ex = Exception.class.cast(obj);
        throw new IOException("Received an exception!", ex);
      } catch (final ClassCastException e1) {
        throw new IOException(String.format("Received an unexpected response! Found: %s; required: %s.", obj.getClass().toString(), clazz.toString()), e0);
      }
    }
  }





  protected SeElementSelectMethod userToReqElementSelectMethod(final ElementSelectMethod userMethod) {
    final SeElementSelectMethod reqMethod;
    switch (userMethod) {
      case CLASS_NAME:        reqMethod = SeElementSelectMethod.CLASS_NAME;        break;
      case CSS_SELECTOR:      reqMethod = SeElementSelectMethod.CSS_SELECTOR;      break;
      case ID:                reqMethod = SeElementSelectMethod.ID;                break;
      case LINK_TEXT:         reqMethod = SeElementSelectMethod.LINK_TEXT;         break;
      case NAME:              reqMethod = SeElementSelectMethod.NAME;              break;
      case PARTIAL_LINK_TEXT: reqMethod = SeElementSelectMethod.PARTIAL_LINK_TEXT; break;
      case TAG_NAME:          reqMethod = SeElementSelectMethod.TAG_NAME;          break;
      case UUID:              reqMethod = SeElementSelectMethod.UUID;              break;
      case XPATH:             reqMethod = SeElementSelectMethod.XPATH;             break;
      default:                throw new RuntimeException("Unexpected error!");
    }

    return reqMethod;
  }

  protected SeOptionSelectMethod userToReqOptionSelectMethod(final OptionSelectMethod userMethod) {
    final SeOptionSelectMethod reqMethod;
    switch (userMethod) {
      case INDEX:        reqMethod = SeOptionSelectMethod.INDEX;        break;
      case VALUE:        reqMethod = SeOptionSelectMethod.VALUE;        break;
      case VISIBLE_TEXT: reqMethod = SeOptionSelectMethod.VISIBLE_TEXT; break;
      default:           throw new RuntimeException("Unexpected error!");
    }

    return reqMethod;
  }

  protected Capture resToUserCapture(final SeResCapture resCapture) {
    return new Capture(
        resCapture.name,
        resCapture.time,
        resToUserCookieList(resCapture.cookieList),
        resToUserWindowList(resCapture.windowList));
  }

  protected CaptureCookie resToUserCookie(final SeCookie resCookie) {
    return new CaptureCookie(
        resCookie.domain,
        resCookie.expiry,
        resCookie.name,
        resCookie.path,
        resCookie.value,
        resCookie.isSecure);
  }

  protected Set<CaptureCookie> resToUserCookieList(final Set<SeCookie> resCookieList) {
    final Set<CaptureCookie> userCookieList = new HashSet<CaptureCookie>();
    for (final SeCookie resCookie : resCookieList) {
      final CaptureCookie cookie = resToUserCookie(resCookie);
      userCookieList.add(cookie);
    }

    return userCookieList;
  }

  protected CaptureWindow resToUserWindow(final SeWindow resWindow) {
    return new CaptureWindow(
        resWindow.title,
        resWindow.url,
        resWindow.handle,
        new Position(resWindow.posX, resWindow.posY),
        new Location(resWindow.width, resWindow.height),
        resWindow.html,
        resWindow.screenshot,
        resToUserFrameList(resWindow.frameList));
  }

  protected List<CaptureWindow> resToUserWindowList(final List<SeWindow> resWindowList) {
    final List<CaptureWindow> userWindowList = new ArrayList<CaptureWindow>();
    for (final SeWindow resWindow : resWindowList) {
      final CaptureWindow window = resToUserWindow(resWindow);
      userWindowList.add(window);
    }

    return userWindowList;
  }

  protected CaptureFrame resToUserFrame(final SeFrame resFrame) {
    return new CaptureFrame(
        resFrame.index,
        resFrame.name,
        resFrame.src,
        resFrame.html,
        resToUserFrameList(resFrame.frameList));
  }

  protected List<CaptureFrame> resToUserFrameList(final List<SeFrame> resFrameList) {
    final List<CaptureFrame> userFrameList = new ArrayList<CaptureFrame>();
    for (final SeFrame resFrame : resFrameList) {
      final CaptureFrame frame = resToUserFrame(resFrame);
      userFrameList.add(frame);
    }

    return userFrameList;
  }




  protected ActorElement resToUserElement(final SeElement resElement) {
    return new ActorElement(
        session,
        resElement.uuid,
        new Position(resElement.posX, resElement.posY),
        new Location(resElement.width, resElement.height),
        resElement.name,
        resElement.text,
        resElement.isDisplayed,
        resElement.isEnabled,
        resElement.isSelected,
        resElement.windowHandle,
        resElement.framePath,
        resElement.attributeList,
        new ArrayList<IElement>());
  }

  protected List<IElement> resToUserElementList(final List<SeElement> resElementList) {
    final List<IElement> userElementList = new ArrayList<IElement>();
    for (final SeElement resElement : resElementList) {
      final ActorElement actorElement = resToUserElement(resElement);
      userElementList.add(actorElement);
    }

    return userElementList;
  }

  protected SeElementSelector userToReqSelector(final ElementSelector userSelector) {
    return new SeElementSelector(userToReqElementSelectMethod(userSelector.method), userSelector.query);
  }

  protected List<SeElementSelector> userToReqSelectorList(final List<ElementSelector> userSelectorList) {
    final List<SeElementSelector> reqSelectorList = new ArrayList<SeElementSelector>();
    for (final ElementSelector userSelector : userSelectorList) {
      final SeElementSelector reqSelector = userToReqSelector(userSelector);
      reqSelectorList.add(reqSelector);
    }

    return reqSelectorList;
  }

  protected List<SePage> userToReqPageList(final List<BrowserPage> userPageList) {
    final List<SePage> reqPageList = new ArrayList<SePage>();
    for (final BrowserPage userPage : userPageList) {
      final SePage reqPage = new SePage(userPage.name, userToReqSelectorList(userPage.selectorList));
      reqPageList.add(reqPage);
    }

    return reqPageList;
  }

  protected ActorSelect resToUserSelect(final SeSelect resSelect) {
    return new ActorSelect(
        session,
        resToUserElement(resSelect.element),
        resSelect.optionCount,
        resSelect.selectedIndex,
        resToUserOption(resSelect.selectedOption),
        resToUserOptionList(resSelect.options));
  }

  protected ActorOption resToUserOption(final SeOption resOption) {
    if (resOption == null) {
      return null;
    } else {
      return new ActorOption(
          session,
          resToUserElement(resOption.element));
    }
  }

  protected List<IOption> resToUserOptionList(final List<SeOption> resOptionList) {
    final List<IOption> userOptionList = new ArrayList<IOption>();
    for (final SeOption resOption : resOptionList) {
      final ActorOption actorOption = resToUserOption(resOption);
      userOptionList.add(actorOption);
    }

    return userOptionList;
  }
}
