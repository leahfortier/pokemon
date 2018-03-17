package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.effect.interfaces.InvokeInterfaces.RepellingEffect;
import map.overworld.wild.WildEncounter;
import util.RandomUtils;

public interface RepelLowLevelEncounterEffect extends RepellingEffect {

    @Override
    default boolean shouldRepel(ActivePokemon playerFront, WildEncounter wildPokemon) {
        return RandomUtils.chanceTest(50) && wildPokemon.getLevel() + 5 <= playerFront.getLevel();
    }
}
