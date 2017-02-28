package pattern.map;

import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import pattern.generic.TriggerMatcher;

import java.util.List;

public class WildBattleMatcher extends TriggerMatcher {
    private String name;
    private EncounterRate encounterRate;
    private WildEncounter[] wildPokemon;

    public WildBattleMatcher(String name, EncounterRate encounterRate, List<WildEncounter> wildEncounters) {
        this.name = name;
        this.encounterRate = encounterRate;
        this.wildPokemon = wildEncounters.toArray(new WildEncounter[0]);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public EncounterRate getEncounterRate() {
        return this.encounterRate;
    }

    public WildEncounter[] getWildEncounters() {
        return this.wildPokemon;
    }
}
