package item.hold;

import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, StatChangingEffect {
	Stat powerStat();
}
