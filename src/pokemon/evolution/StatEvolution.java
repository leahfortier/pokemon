package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;
import pokemon.Stat;
import pokemon.species.PokemonNamesies;

class StatEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private final BaseEvolution evolution;
    private final boolean equals;
    private final Stat higher;
    private final Stat lower;

    StatEvolution(String equals, String higher, String lower, BaseEvolution evolution) {
        this.evolution = evolution;
        this.equals = equals.equals("Equal"); // Equality ftw

        this.higher = Stat.valueOf(higher.toUpperCase());
        this.lower = Stat.valueOf(lower.toUpperCase());
    }

    @Override
    public BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
        int high = p.getStat(higher);
        int low = p.getStat(lower);

        if (equals && high == low) {
            return evolution.getEvolution(type, p, use);
        }

        if (high > low) {
            return evolution.getEvolution(type, p, use);
        }

        return null;
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
