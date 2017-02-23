package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.Stat;

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
        int[] stats = p.getStats();
        int high = stats[higher.index()];
        int low = stats[lower.index()];

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
    public String toString() {
        return EvolutionType.STAT + " " + (equals ? "Equal" : "Higher") + " " + higher + " " + lower + " " + evolution;
    }
}
