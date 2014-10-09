package item.hold;

import main.Type;
import battle.effect.PowerChangeEffect;

public interface GemItem extends HoldItem, ConsumableItem, PowerChangeEffect
{
	public Type getType();
}
