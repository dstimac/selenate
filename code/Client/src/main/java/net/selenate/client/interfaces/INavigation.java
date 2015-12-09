package net.selenate.client.interfaces;

import java.io.IOException;

public interface INavigation {
  public void back() throws IOException;
  public void forward() throws IOException;
  public void refresh() throws IOException;
}
