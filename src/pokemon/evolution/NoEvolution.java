package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;
import pokemon.PokemonNamesies;

public class NoEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    @Override
    public BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
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

    @Override
    public String getString() {
        return "Does not evolve";
    }

    @Override
    public String toString() {
        return EvolutionType.NONE.name();
    }
}
