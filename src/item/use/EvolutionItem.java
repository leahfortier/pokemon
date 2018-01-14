package item.use;

import battle.ActivePokemon;
import pokemon.evolution.EvolutionMethod;

public interface EvolutionItem extends PokemonUseItem {
    @Override
    default boolean use(ActivePokemon p) {
        return EvolutionMethod.ITEM.checkEvolution(p, this.namesies());
    }
}
