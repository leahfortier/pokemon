package item.use;

import battle.Battle;
import battle.attack.Move;
import pokemon.ActivePokemon;

public interface PokemonUseItem extends UseItem {
    boolean use(ActivePokemon p);

    @Override
    default boolean use(Battle b, ActivePokemon p, Move m) {
        return this.use(p);
    }
}
