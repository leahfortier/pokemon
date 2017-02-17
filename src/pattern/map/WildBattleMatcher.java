package pattern.map;

import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.List;

public class WildBattleMatcher extends MultiPointTriggerMatcher {
    private String name;
    private EncounterRate encounterRate;
    private WildEncounter[] wildPokemon;

    public WildBattleMatcher(String name, EncounterRate encounterRate, List<WildEncounter> wildEncounters) {
        this.name = name;
        this.encounterRate = encounterRate;
        this.wildPokemon = wildEncounters.toArray(new WildEncounter[0]);
    }

    public EncounterRate getEncounterRate() {
        return this.encounterRate;
    }

    public WildEncounter[] getWildEncounters() {
        return this.wildPokemon;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.WILD_BATTLE;
    }

    @Override
    public String getBasicName() {
        return this.name;
    }
}
