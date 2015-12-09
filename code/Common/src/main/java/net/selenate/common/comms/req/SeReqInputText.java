package net.selenate.common.comms.req;

import java.util.List;

import net.selenate.common.comms.*;

public class SeReqInputText implements SeCommsReq {
  private static final long serialVersionUID = 1L;

  public final String         windowHandle;
  public final List<Integer>  framePath;
  public final SeElementSelectMethod method;
  public final String         query;
  public final String         text;

  public SeReqInputText(
      final String                windowHandle,
      final List<Integer>         framePath,
      final SeElementSelectMethod method,
      final String                query,
      final String                text) {
    if (windowHandle == null) {
      throw new IllegalArgumentException("Window handle cannot be null!");
    }
    if (framePath == null) {
      throw new IllegalArgumentException("Frame path cannot be null!");
    }
    if (method == null) {
      throw new IllegalArgumentException("Method cannot be null!");
    }
    if (query == null) {
      throw new IllegalArgumentException("Query cannot be null!");
    }
    if (text == null) {
      throw new IllegalArgumentException("Text cannot be null!");
    }

    this.windowHandle = windowHandle;
    this.framePath    = framePath;
    this.method       = method;
    this.query        = query;
    this.text         = text;
  }

  public SeReqInputText(
      final SeFrameElementSelector selector,
      final String                 text) {
    if (selector == null) {
      throw new IllegalArgumentException("Selector cannot be null!");
    }
    if (text == null) {
      throw new IllegalArgumentException("Text cannot be null!");
    }

    this.windowHandle = selector.windowHandle;
    this.framePath    = selector.framePath;
    this.method       = selector.method;
    this.query        = selector.query;
    this.text         = text;
  }

  @Override
  public String toString() {
    String framePathStr = "[";
    boolean isFirst = true;
    for (int f : framePath) {
      framePathStr += (isFirst ? "" : ":") + f;
      isFirst = false;
    }
    framePathStr += "]";
    return String.format("SeReqInputText([%s] %s: %s, %s, %s)", windowHandle, framePathStr, method, query, text);
  }
}
