package pattern.map;

import map.overworld.WildEncounterInfo;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.List;

public class FishingMatcher extends MultiPointTriggerMatcher {
    private String name;
    private int minLevel;
    private int maxLevel;
    private WildEncounterInfo[] wildPokemon;

    public FishingMatcher(String name, int minLevel, int maxLevel, WildEncounterInfo[] wildEncounters) {
        this.name = name;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.wildPokemon = wildEncounters;
    }

    public FishingMatcher(String name, int minLevel, int maxLevel, List<WildEncounterInfo> wildEncounters) {
        this(name, minLevel, maxLevel, wildEncounters.toArray(new WildEncounterInfo[0]));
    }

    public FishingMatcher(String name, WildEncounterInfo[] wildEncounters) {
        this(name, 0, 0, wildEncounters);
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

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }
}
