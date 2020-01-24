package item.use;

import battle.ActivePokemon;
import battle.Battle;
import item.hold.HoldItem;

public interface BallItem extends HoldItem {
    default double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
        return 1;
    }

    default int getAdditive(ActivePokemon me, ActivePokemon o, Battle b) {
        return 0;
    }

    default void afterCaught(ActivePokemon p) {}
}
