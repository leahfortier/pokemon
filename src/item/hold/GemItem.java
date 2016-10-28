package item.hold;

import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import main.Type;

public interface GemItem extends HoldItem, ConsumableItem, PowerChangeEffect {
	Type getType();
}
