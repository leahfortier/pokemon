package item.hold;

import battle.effect.InvokeInterfaces.SimpleStatModifyingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, SimpleStatModifyingEffect {
    Stat powerStat();
}
