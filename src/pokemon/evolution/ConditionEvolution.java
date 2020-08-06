package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;
import pokemon.species.PokemonNamesies;

public abstract class ConditionEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    protected final BaseEvolution evolution;

    protected ConditionEvolution(BaseEvolution evolution) {
        this.evolution = evolution;
    }

    public BaseEvolution getEvolution() {
        return this.evolution;
    }

    protected abstract boolean meetsCondition(ActivePokemon pokemon);

    @Override
    public final BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon pokemon, ItemNamesies use) {
        if (this.meetsCondition(pokemon)) {
            return this.evolution.getEvolution(type, pokemon, use);
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }
}
