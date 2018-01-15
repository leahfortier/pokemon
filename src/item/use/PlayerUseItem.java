package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;

public interface PlayerUseItem extends UseItem {
    boolean use();

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use();
    }
}
