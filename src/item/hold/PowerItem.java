package item.hold;

import battle.effect.generic.EffectInterfaces.StatModifyingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, StatModifyingEffect {
	Stat powerStat();
}
