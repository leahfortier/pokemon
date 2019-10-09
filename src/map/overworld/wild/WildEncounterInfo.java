package map.overworld.wild;

import battle.ActivePokemon;
import battle.effect.InvokeInterfaces.WildEncounterAlterer;
import battle.effect.InvokeInterfaces.WildEncounterSelector;
import main.Game;
import pokemon.species.PokemonNamesies;
import util.RandomUtils;

import java.util.Arrays;

public class WildEncounterInfo {
    private final PokemonNamesies pokemon;

    private final int minLevel;
    private final int maxLevel;

    private final int probability;

    public WildEncounterInfo(String pokemon, int minLevel, int maxLevel, String probability) {
        this(
                PokemonNamesies.getValueOf(pokemon),
                minLevel,
                maxLevel,
                Integer.parseInt(probability)
        );
    }

    public WildEncounterInfo(PokemonNamesies pokemon, int minLevel, int maxLevel, int probability) {
        this.pokemon = pokemon;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.probability = probability;
    }

    public PokemonNamesies getPokemonName() {
        return this.pokemon;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getProbability() {
        return this.probability;
    }

    public static WildEncounter getWildEncounter(ActivePokemon playerFront, WildEncounterInfo[] wildEncounters) {
        WildEncounterInfo encounterInfo = getWildEncounterInfo(wildEncounters);
        WildEncounter encounter = new WildEncounter(encounterInfo);
        WildEncounterAlterer.invokeWildEncounterAlterer(playerFront, encounterInfo, encounter);

        return encounter;
    }

    private static WildEncounterInfo getWildEncounterInfo(WildEncounterInfo[] wildEncounters) {
        ActivePokemon front = Game.getPlayer().front();
        WildEncounterInfo forcedEncounter = WildEncounterSelector.getForcedWildEncounter(front, wildEncounters);
        if (forcedEncounter != null) {
            return forcedEncounter;
        }

        return RandomUtils.getPercentageValue(Arrays.asList(wildEncounters), WildEncounterInfo::getProbability);
    }
}
