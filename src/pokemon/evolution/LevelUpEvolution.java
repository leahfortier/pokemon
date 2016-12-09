package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;

class LevelUpEvolution extends BaseEvolution {
    private static final long serialVersionUID = 1L;

    private final int level;

    LevelUpEvolution(int num, int level) {
        super(EvolutionMethod.LEVEL, num);

        this.level = level;
    }

    @Override
    public Evolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem) {
        if (toEvolve.getLevel() >= level) {
            return this;
        }

        return null;
    }
}
