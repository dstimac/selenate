package net.selenate.client.interfaces;

import net.selenate.common.comms.res.SeResDownloadFile;
import net.selenate.common.user.Location;
import net.selenate.common.user.Position;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IElement {
  public String   getUuid();
  public Position getPos();
  public Location getLoc();
  public String   getName();
  public String   getText();
  public boolean  getIsDisplayed();
  public boolean  getIsEnabled();
  public boolean  getIsSelected();
  public String   getWindowHandle();
  public List<Integer>       getFramePath();
  public Map<String, String> getAttributeList();
  public List<IElement>      getChildren();

  public ISelect toSelect() throws IOException;

  public void clearText() throws IOException;
  public void appendText(String text) throws IOException;
  public void setText(String text) throws IOException;
  public void inputText(String text) throws IOException;
  public void click() throws IOException;

  /**
   * Invokes remote file download and output to SeResDownloadFile
   * @param  selector          link with download attachment action
   * @return SeResDownloadFile contains binary data file and fileExtension
   * @throws IOException
   */
  public SeResDownloadFile downloadFile() throws IOException;

  public byte[] capture() throws IOException;
}
