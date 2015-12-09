package net.selenate.client.user;

import akka.actor.ActorRef;
import net.selenate.client.interfaces.IElement;
import net.selenate.client.interfaces.ISelect;
import net.selenate.common.comms.SeElementSelectMethod;
import net.selenate.common.comms.req.SeReqAppendText;
import net.selenate.common.comms.req.SeReqCaptureElement;
import net.selenate.common.comms.req.SeReqClearText;
import net.selenate.common.comms.req.SeReqClick;
import net.selenate.common.comms.req.SeReqDownloadFile;
import net.selenate.common.comms.req.SeReqFindSelect;
import net.selenate.common.comms.req.SeReqInputText;
import net.selenate.common.comms.res.SeResAppendText;
import net.selenate.common.comms.res.SeResCaptureElement;
import net.selenate.common.comms.res.SeResClearText;
import net.selenate.common.comms.res.SeResClick;
import net.selenate.common.comms.res.SeResDownloadFile;
import net.selenate.common.comms.res.SeResFindSelect;
import net.selenate.common.comms.res.SeResInputText;
import net.selenate.common.user.Location;
import net.selenate.common.user.Position;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ActorElement extends ActorBase implements IElement {
  private final String   uuid;
  private final Position pos;
  private final Location loc;
  private final String   name;
  private final String   text;
  private final boolean  isDisplayed;
  private final boolean  isEnabled;
  private final boolean  isSelected;
  private final String   windowHandle;
  private final List<Integer>       framePath;
  private final Map<String, String> attributeList;
  private final List<IElement>      children;

  public ActorElement(
      final ActorRef session,
      final String   uuid,
      final Position pos,
      final Location loc,
      final String   name,
      final String   text,
      final boolean  isDisplayed,
      final boolean  isEnabled,
      final boolean  isSelected,
      final String   windowHandle,
      final List<Integer>       framePath,
      final Map<String, String> attributeList,
      final List<IElement>      children) {
    super(session);

    if (uuid == null) {
      throw new IllegalArgumentException("UUID cannot be null!");
    }
    if (pos == null) {
      throw new IllegalArgumentException("Position cannot be null!");
    }
    if (loc == null) {
      throw new IllegalArgumentException("Location cannot be null!");
    }
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null!");
    }
    if (text == null) {
      throw new IllegalArgumentException("Text cannot be null!");
    }
    if (windowHandle == null) {
      throw new IllegalArgumentException("WindowHandle cannot be null!");
    }
    if (framePath == null) {
      throw new IllegalArgumentException("Frame path cannot be null!");
    }
    if (attributeList == null) {
      throw new IllegalArgumentException("Attribute list cannot be null!");
    }
    if (children == null) {
      throw new IllegalArgumentException("Children cannot be null!");
    }

    this.uuid          = uuid;
    this.pos           = pos;
    this.loc           = loc;
    this.name          = name;
    this.text          = text;
    this.isDisplayed   = isDisplayed;
    this.isEnabled     = isEnabled;
    this.isSelected    = isSelected;
    this.windowHandle  = windowHandle;
    this.framePath     = framePath;
    this.attributeList = attributeList;
    this.children      = children;
  }


  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public Position getPos() {
    return pos;
  }

  @Override
  public Location getLoc() {
    return loc;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public boolean getIsDisplayed() {
    return isDisplayed;
  }

  @Override
  public boolean getIsEnabled() {
    return isEnabled;
  }

  @Override
  public boolean getIsSelected() {
    return isSelected;
  }

  @Override
  public List<Integer> getFramePath() {
    return framePath;
  }

  @Override
  public Map<String, String> getAttributeList() {
    return attributeList;
  }

  @Override
  public List<IElement> getChildren() {
    return children;
  }

  @Override
  public String getWindowHandle() {
    return windowHandle;
  }

  @Override
  public ISelect toSelect() throws IOException {
    final SeResFindSelect res = typedBlock(new SeReqFindSelect(SeElementSelectMethod.UUID, uuid), SeResFindSelect.class);
    return resToUserSelect(res.select);
  }

  @Override
  public void clearText() throws IOException {
    typedBlock(new SeReqClearText(windowHandle, framePath, SeElementSelectMethod.UUID, uuid), SeResClearText.class);
  }

  @Override
  public void appendText(final String text) throws IOException {
    typedBlock(new SeReqAppendText(windowHandle, framePath, SeElementSelectMethod.UUID, uuid, text), SeResAppendText.class);
  }

  @Override
  public void setText(final String text) throws IOException {
    clearText();
    appendText(text);
  }

  @Override
  public void inputText(String text) throws IOException {
    typedBlock(new SeReqInputText(windowHandle, framePath, SeElementSelectMethod.UUID, uuid, text), SeResInputText.class);
  }

  @Override
  public void click() throws IOException {
    typedBlock(new SeReqClick(windowHandle, framePath, SeElementSelectMethod.UUID, uuid), SeResClick.class);
  }

  @Override
  public SeResDownloadFile downloadFile() throws IOException {
    return typedBlock(new SeReqDownloadFile(windowHandle, framePath, SeElementSelectMethod.UUID, uuid), SeResDownloadFile.class);
  }

  @Override
  public byte[] capture() throws IOException {
    final SeResCaptureElement res = typedBlock(new SeReqCaptureElement(windowHandle, framePath, SeElementSelectMethod.UUID, uuid), SeResCaptureElement.class);
    return res.body;
  }

}
