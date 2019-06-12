package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import item.use.UseItem.BagUseItem;

public interface PlayerUseItem extends BagUseItem {
    boolean use();

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use();
    }
}
