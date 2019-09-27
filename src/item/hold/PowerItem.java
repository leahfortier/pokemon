package item.hold;

import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import pokemon.stat.Stat;

public interface PowerItem extends EVItem, SimpleStatModifyingEffect {
    Stat powerStat();
}
