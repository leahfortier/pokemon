package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;

public interface MoveUseItem extends UseItem {
    boolean use(ActivePokemon p, Move m);

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use(p, m);
    }
}
