package item.hold;

import main.Type;
import battle.effect.PowerChangeEffect;

public interface PlateItem extends HoldItem, PowerChangeEffect {
	Type getType();
}
