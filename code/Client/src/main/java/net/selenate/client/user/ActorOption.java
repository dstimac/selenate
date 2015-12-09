package net.selenate.client.user;

import akka.actor.ActorRef;
import net.selenate.client.interfaces.IElement;
import net.selenate.client.interfaces.IOption;
import net.selenate.common.user.Location;
import net.selenate.common.user.Position;

import java.util.List;
import java.util.Map;

public class ActorOption extends ActorElement implements IOption {
  public ActorOption(
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
      final IOption  selectedOption,
      final List<IOption>       options,
      final List<Integer>       framePath,
      final Map<String, String> attributeList,
      final List<IElement>      children) {
    super(session, uuid, pos, loc, name, text, isDisplayed, isEnabled, isSelected, windowHandle, framePath, attributeList, children);
  }

  public ActorOption(
      final ActorRef session,
      final IElement element) {
    super(session, element.getUuid(), element.getPos(), element.getLoc(), element.getName(), element.getText(), element.getIsDisplayed(), element.getIsEnabled(), element.getIsSelected(), element.getWindowHandle(), element.getFramePath(), element.getAttributeList(), element.getChildren());
  }
}
