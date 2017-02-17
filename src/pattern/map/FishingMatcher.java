package pattern.map;

import map.overworld.WildEncounter;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.List;

public class FishingMatcher extends MultiPointTriggerMatcher {
    private String name;
    private WildEncounter[] wildPokemon;

    public FishingMatcher(String name, WildEncounter[] wildEncounters) {
        this.name = name;
        this.wildPokemon = wildEncounters;
    }

    public FishingMatcher(String name, List<WildEncounter> wildEncounters) {
        this(name, wildEncounters.toArray(new WildEncounter[0]));
    }

    public WildEncounter[] getWildEncounters() {
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
