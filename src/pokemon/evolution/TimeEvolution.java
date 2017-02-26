package pokemon.evolution;

import item.ItemNamesies;
import map.daynight.DayCycle;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

public class TimeEvolution extends Evolution {
    private final BaseEvolution evolution;
    private final DayCycle timeOfDay;

    TimeEvolution(String timeOfDay, BaseEvolution evolution) {
        this.evolution = evolution;
        this.timeOfDay = DayCycle.valueOf(timeOfDay);
    }

    @Override
    public BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon pokemon, ItemNamesies use) {
        if (DayCycle.getTimeOfDay() == this.timeOfDay) {
            return this.evolution.getEvolution(type, pokemon, use);
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }

    @Override
    public String toString() {
        return EvolutionType.TIME + " " + timeOfDay.name() + " " + evolution.toString();
    }
}
