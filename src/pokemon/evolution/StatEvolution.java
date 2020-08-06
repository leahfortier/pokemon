package pokemon.evolution;

import battle.ActivePokemon;
import pokemon.species.PokemonNamesies;
import pokemon.stat.Stat;

class StatEvolution extends ConditionEvolution {
    private static final long serialVersionUID = 1L;

    private final boolean equals;
    private final Stat higher;
    private final Stat lower;

    StatEvolution(String equals, String higher, String lower, BaseEvolution evolution) {
        super(evolution);
        this.equals = equals.equals("Equal"); // Equality ftw

        this.higher = Stat.valueOf(higher.toUpperCase());
        this.lower = Stat.valueOf(lower.toUpperCase());
    }

    @Override
    protected boolean meetsCondition(ActivePokemon pokemon) {
        int high = pokemon.getStat(higher);
        int low = pokemon.getStat(lower);

        return (equals && high == low) || (high > low);
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return evolution.getEvolutions();
    }

    @Override
    public String getString() {
        return this.evolution.getString() + ", " + lower.getName() + " " + (equals ? "=" : "<") + " " + higher.getName();
    }

    @Override
    public String toString() {
        return EvolutionType.STAT + " " + (equals ? "Equal" : "Higher") + " " + higher + " " + lower + " " + evolution;
    }
}
