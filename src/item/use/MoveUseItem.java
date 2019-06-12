package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import item.use.UseItem.BagUseItem;

public interface MoveUseItem extends BagUseItem {
    boolean use(ActivePokemon p, Move m);

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use(p, m);
    }
}
