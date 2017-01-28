package item.hold;

import battle.effect.SimpleStatModifyingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, SimpleStatModifyingEffect {
	Stat powerStat();
}
