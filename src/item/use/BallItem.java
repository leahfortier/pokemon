package item.use;

import battle.Battle;
import item.ItemInterface;
import pokemon.ActivePokemon;

public interface BallItem extends ItemInterface {
    default double getModifier(ActivePokemon me, ActivePokemon o, Battle b) {
        return 1;
    }

    default int getAdditive(ActivePokemon me, ActivePokemon o, Battle b) {
        return 0;
    }

    default void afterCaught(ActivePokemon p) {}
}
