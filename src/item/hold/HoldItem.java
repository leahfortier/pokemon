package item.hold;

import battle.Battle;
import item.ItemInterface;
import pokemon.ActivePokemon;

public interface HoldItem extends ItemInterface {
    default int flingDamage() {
        return 30;
    }

    default void flingEffect(Battle b, ActivePokemon pelted) {}
}
