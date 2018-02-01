package pattern.map;

import map.overworld.WildEncounterInfo;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.List;

public class FishingMatcher extends MultiPointTriggerMatcher {
    private String name;
    private WildEncounterInfo[] wildPokemon;

    public FishingMatcher(String name, WildEncounterInfo[] wildEncounters) {
        this.name = name;
        this.wildPokemon = wildEncounters;
    }

    public FishingMatcher(String name, List<WildEncounterInfo> wildEncounters) {
        this(name, wildEncounters.toArray(new WildEncounterInfo[0]));
    }

    public WildEncounterInfo[] getWildEncounters() {
        return this.wildPokemon;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.FISHING;
    }

    @Override
    public String getBasicName() {
        return name;
    }
}
