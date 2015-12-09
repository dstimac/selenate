package net.selenate.client.interfaces;

import net.selenate.common.user.OptionSelectMethod;

import java.io.IOException;
import java.util.List;

public interface ISelect extends IElement {
  public void selectByIndex(int id) throws IOException;
  public void select(OptionSelectMethod method, String query) throws IOException;

  public int getOptionCount();
  public Integer getSelectedIndex();
  public IOption getSelectedOption();

  public List<IOption> getOptions();
}
