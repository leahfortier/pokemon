package pattern.map;

import map.overworld.EncounterRate;
import map.overworld.WildEncounterInfo;
import pattern.generic.TriggerMatcher;

import java.util.List;

public class WildBattleMatcher extends TriggerMatcher {
    private String name;
    private EncounterRate encounterRate;
    private WildEncounterInfo[] wildPokemon;
    private int minLevel;
    private int maxLevel;

    public WildBattleMatcher(
            String name,
            EncounterRate encounterRate,
            int minLevel,
            int maxLevel,
            List<WildEncounterInfo> wildEncounters) {
        this.name = name;
        this.encounterRate = encounterRate;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.wildPokemon = wildEncounters.toArray(new WildEncounterInfo[0]);
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

    public WildEncounterInfo[] getWildEncounters() {
        return this.wildPokemon;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }
}
