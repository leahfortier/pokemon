package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.effect.interfaces.InvokeInterfaces.WildEncounterSelector;
import map.overworld.wild.WildEncounterInfo;
import pokemon.species.PokemonInfo;
import type.Type;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public interface TypedWildEncounterSelector extends WildEncounterSelector {
    Type getEncounterType();

    @Override
    default WildEncounterInfo getWildEncounter(ActivePokemon playerFront, WildEncounterInfo[] wildEncounters) {
        if (RandomUtils.chanceTest(50)) {
            List<WildEncounterInfo> typedList = new ArrayList<>();
            for (WildEncounterInfo wildEncounter : wildEncounters) {
                PokemonInfo pokemon = wildEncounter.getPokemonName().getInfo();
                if (pokemon.isType(this.getEncounterType())) {
                    typedList.add(wildEncounter);
                }
            }

            if (!typedList.isEmpty()) {
                return RandomUtils.getRandomValue(typedList);
            }
        }

        return null;
    }
}
