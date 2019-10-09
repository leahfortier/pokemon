package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;
import pokemon.species.PokemonNamesies;

public abstract class BaseEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private final EvolutionMethod evolutionMethod;
    private final PokemonNamesies evolutionNamesies;

    protected BaseEvolution(EvolutionMethod evolutionMethod, String namesies) {
        this.evolutionMethod = evolutionMethod;
        this.evolutionNamesies = PokemonNamesies.valueOf(namesies);
    }

    protected abstract BaseEvolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem);

    public final PokemonNamesies getEvolution() {
        return evolutionNamesies;
    }

    @Override
    public final PokemonNamesies[] getEvolutions() {
        return new PokemonNamesies[] { this.getEvolution() };
    }

    @Override
    public final BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
        if (type != this.evolutionMethod) {
            return null;
        }

        return this.getEvolution(p, use);
    }
}
