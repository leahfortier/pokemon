package pokemon.evolution;

import battle.ActivePokemon;
import map.daynight.DayCycle;
import pokemon.species.PokemonNamesies;
import util.string.StringUtils;

public class TimeEvolution extends ConditionEvolution {
    private static final long serialVersionUID = 1L;

    private final DayCycle timeOfDay;

    TimeEvolution(String timeOfDay, BaseEvolution evolution) {
        super(evolution);
        this.timeOfDay = DayCycle.valueOf(timeOfDay);
    }

    @Override
    protected boolean meetsCondition(ActivePokemon pokemon) {
        return DayCycle.getTimeOfDay() == this.timeOfDay;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }

    @Override
    public String getString() {
        return this.evolution.getString() + ", " + StringUtils.properCase(this.timeOfDay.name().toLowerCase()) + " only";
    }

    @Override
    public String toString() {
        return EvolutionType.TIME + " " + timeOfDay.name() + " " + evolution;
    }
}
