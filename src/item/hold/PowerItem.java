package item.hold;

import pokemon.Stat;
import battle.effect.StatChangingEffect;

public interface PowerItem extends EVItem, StatChangingEffect {
	Stat powerStat();
}
