package net.selenate.client.user;

import akka.actor.ActorRef;
import net.selenate.client.interfaces.IElement;
import net.selenate.client.interfaces.IOption;
import net.selenate.client.interfaces.ISelect;
import net.selenate.common.comms.SeElementSelectMethod;
import net.selenate.common.comms.SeOptionSelectMethod;
import net.selenate.common.comms.req.SeReqSelectOption;
import net.selenate.common.comms.res.SeResSelectOption;
import net.selenate.common.user.Location;
import net.selenate.common.user.OptionSelectMethod;
import net.selenate.common.user.Position;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ActorSelect extends ActorElement implements ISelect {

  public final int       optionCount;
  public final Integer   selectedIndex;
  public final IOption   selectedOption;
  public final List<IOption> options;

  public ActorSelect(
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
      final int      optionCount,
      final Integer  selectedIndex,
      final IOption selectedOption,
      final List<IOption>       options,
      final List<Integer>       framePath,
      final Map<String, String> attributeList,
      final List<IElement>      children) {
    super(session, uuid, pos, loc, name, text, isDisplayed, isEnabled, isSelected, windowHandle, framePath, attributeList, children);
    this.optionCount    = optionCount;
    this.selectedIndex  = selectedIndex;
    this.selectedOption = selectedOption;
    this.options        = options;
  }

  public ActorSelect(
      final ActorRef session,
      final IElement element,
      final int      optionCount,
      final Integer  selectedIndex,
      final IOption selectedOption,
      final List<IOption> options) {
    super(session, element.getUuid(), element.getPos(), element.getLoc(), element.getName(), element.getText(), element.getIsDisplayed(), element.getIsEnabled(), element.getIsSelected(), element.getWindowHandle(), element.getFramePath(), element.getAttributeList(), element.getChildren());
    this.optionCount    = optionCount;
    this.selectedIndex  = selectedIndex;
    this.selectedOption = selectedOption;
    this.options        = options;
  }

  @Override
  public void select(final OptionSelectMethod method, final String query)
      throws IOException {
    final SeOptionSelectMethod reqMethod = userToReqOptionSelectMethod(method);
    typedBlock(new SeReqSelectOption(getWindowHandle(), getFramePath(), SeElementSelectMethod.UUID, getUuid(), reqMethod, query), SeResSelectOption.class);
  }

  @Override
  public void selectByIndex(final int index) throws IOException {
    select(OptionSelectMethod.INDEX, Integer.toString(index));
  }

  @Override
  public int getOptionCount() {
    return optionCount;
  }

  @Override
  public Integer getSelectedIndex() {
    return selectedIndex;
  }

  @Override
  public IOption getSelectedOption() {
    return selectedOption;
  }

  @Override
  public List<IOption> getOptions() {
    return options;
  }
}
