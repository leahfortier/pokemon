package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;

class LevelUpEvolution extends BaseEvolution {
    private static final long serialVersionUID = 1L;

    private final int level;

    LevelUpEvolution(String namesies, int level) {
        super(EvolutionMethod.LEVEL, namesies);

        this.level = level;
    }

    @Override
    public BaseEvolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem) {
        if (toEvolve.getLevel() >= level) {
            return this;
        }

        return null;
    }

    @Override
    public String toString() {
        return EvolutionType.LEVEL + " " + super.getEvolution().namesies() + " " + this.level;
    }

    @Override
    public String getString() {
        return "Level " + level;
    }
}
