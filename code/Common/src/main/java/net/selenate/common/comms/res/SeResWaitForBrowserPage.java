package net.selenate.common.comms.res;

import net.selenate.common.comms.SePage;

public class SeResWaitForBrowserPage implements SeCommsRes {
  private static final long serialVersionUID = 1L;

  public final boolean isSuccessful;
  public final SePage  foundPage;

  public SeResWaitForBrowserPage(
      final boolean isSuccessful,
      final SePage  foundPage) {
    if (isSuccessful && foundPage == null) {
      throw new IllegalArgumentException("When successful, found page cannot be null!");
    }
    if (!isSuccessful && foundPage != null) {
      throw new IllegalArgumentException("When unsuccessful, found page must be null!");
    }

    this.isSuccessful = isSuccessful;
    this.foundPage    = foundPage;
  }

  @Override
  public String toString() {
    final String foundPageName = (foundPage == null) ? null : foundPage.name;
    return String.format("SeResWaitForBrowserPage(%s, %s)",
        isSuccessful, foundPageName);
  }
}

