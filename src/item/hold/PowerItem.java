package item.hold;

import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, SimpleStatModifyingEffect {
    Stat powerStat();
}
