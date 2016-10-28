package item.hold;

import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import main.Type;

public interface PlateItem extends HoldItem, PowerChangeEffect {
	Type getType();
}
