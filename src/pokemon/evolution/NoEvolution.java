package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

class NoEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    @Override
    public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, ItemNamesies use) {
        return null;
    }

    @Override
    public boolean canEvolve() {
        return false;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return new PokemonNamesies[0];
    }
}
