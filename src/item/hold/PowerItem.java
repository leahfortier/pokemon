package item.hold;

import battle.effect.interfaces.SimpleStatModifyingEffect;
import pokemon.Stat;

public interface PowerItem extends EVItem, SimpleStatModifyingEffect {
    Stat powerStat();
}
