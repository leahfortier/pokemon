package map.overworld;

import battle.ActivePokemon;
import battle.effect.generic.EffectInterfaces.WildEncounterSelector;
import main.Game;
import pokemon.PokemonNamesies;
import util.GeneralUtils;

import java.util.Arrays;

public class WildEncounterInfo {
    private PokemonNamesies pokemon;

    private int minLevel;
    private int maxLevel;

    private int probability;

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

    public static WildEncounterInfo getWildEncounterInfo(WildEncounterInfo[] wildEncounters) {
        ActivePokemon front = Game.getPlayer().front();
        WildEncounterInfo forcedEncounter = WildEncounterSelector.getForcedWildEncounter(front, wildEncounters);
        if (forcedEncounter != null) {
            return forcedEncounter;
        }

        return GeneralUtils.getPercentageValue(Arrays.asList(wildEncounters), WildEncounterInfo::getProbability);
    }
}
