package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;

public interface BattleUseItem extends UseItem {
    boolean use(ActivePokemon p, Battle b);

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use(p, b);
    }
}
