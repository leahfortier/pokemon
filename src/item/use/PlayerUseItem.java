package item.use;

import battle.Battle;
import battle.attack.Move;
import battle.ActivePokemon;

public interface PlayerUseItem extends UseItem {
    boolean use();

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use();
    }
}
