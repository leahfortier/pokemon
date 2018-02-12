package item.use;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;

// Can be used on a Pokemon inside battle and outside of battle
public interface BattlePokemonUseItem extends PokemonUseItem, BattleUseItem {
    @Override
    default boolean use(ActivePokemon p) {
        return this.use(p, null);
    }

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return b == null ? use(p) : use(p, b);
    }
}
