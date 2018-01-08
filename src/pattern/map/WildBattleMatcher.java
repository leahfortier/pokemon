package pattern.map;

import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import pattern.generic.TriggerMatcher;

import java.util.List;

public class WildBattleMatcher extends TriggerMatcher {
    private String name;
    private EncounterRate encounterRate;
    private WildEncounter[] wildPokemon;
    private int minLevel;
    private int maxLevel;

    public WildBattleMatcher(
            String name,
            EncounterRate encounterRate,
            int minLevel,
            int maxLevel,
            List<WildEncounter> wildEncounters) {
        this.name = name;
        this.encounterRate = encounterRate;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.wildPokemon = wildEncounters.toArray(new WildEncounter[0]);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EncounterRate getEncounterRate() {
        return this.encounterRate;
    }

    public WildEncounter[] getWildEncounters() {
        return this.wildPokemon;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }
}
