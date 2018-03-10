package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;
import pokemon.PokemonNamesies;
import util.Serializable;

public abstract class Evolution implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use);
    public abstract PokemonNamesies[] getEvolutions();
    public abstract String getString();

    public boolean canEvolve() {
        return true;
    }
}
