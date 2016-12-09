package pokemon.evolution;

import item.ItemNamesies;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.Stat;

class StatEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private LevelUpEvolution evolution;
    private boolean equals;
    private Stat higher;
    private Stat lower;

    StatEvolution(String equals, String higher, String lower, Evolution evolution) {
        if (!(evolution instanceof LevelUpEvolution)) {
            Global.error("Stat evolutions must be level up");
            return;
        }

        this.evolution = (LevelUpEvolution)evolution;
        this.equals = equals.equals("Equal"); // Equality ftw

        this.higher = Stat.valueOf(higher.toUpperCase());
        this.lower = Stat.valueOf(lower.toUpperCase());
    }

    @Override
    public Evolution getEvolution(EvolutionCheck type, ActivePokemon p, ItemNamesies use) {
        if (type != EvolutionCheck.LEVEL) {
            return null;
        }

        int[] stats = p.getStats();
        int high = stats[higher.index()], low = stats[lower.index()];

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
}
