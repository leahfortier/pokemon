package item.hold;

import battle.effect.generic.EffectInterfaces.SimpleStatModifyingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, SimpleStatModifyingEffect {
    Stat powerStat();
}
