package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.effect.interfaces.InvokeInterfaces.WildEncounterAlterer;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import util.RandomUtils;

public interface MaxLevelWildEncounterEffect extends WildEncounterAlterer {

    @Override
    default void alterWildPokemon(ActivePokemon playerFront, WildEncounterInfo encounterData, WildEncounter encounter) {
        if (RandomUtils.chanceTest(50)) {
            encounter.setLevel(encounterData.getMaxLevel());
        }
    }
}
