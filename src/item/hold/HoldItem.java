package item.hold;

import battle.ActivePokemon;
import battle.Battle;
import item.ItemInterface;

public interface HoldItem extends ItemInterface {
    default int flingDamage() {
        return 30;
    }

    default void flingEffect(Battle b, ActivePokemon pelted) {}
}
