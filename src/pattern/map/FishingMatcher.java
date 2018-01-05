package pattern.map;

import map.overworld.WildEncounter;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.MultiPointTriggerMatcher;

import java.util.List;

public class FishingMatcher extends MultiPointTriggerMatcher {
    private String name;
    private int minLevel;
    private int maxLevel;
    private WildEncounter[] wildPokemon;
    
    public FishingMatcher(String name, int minLevel, int maxLevel, WildEncounter[] wildEncounters) {
        this.name = name;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.wildPokemon = wildEncounters;
    }
    
    public FishingMatcher(String name, int minLevel, int maxLevel, List<WildEncounter> wildEncounters) {
        this(name, minLevel, maxLevel, wildEncounters.toArray(new WildEncounter[0]));
    }
    
    public FishingMatcher(String name, WildEncounter[] wildEncounters) {
        this(name, 0, 0, wildEncounters);
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
    
    public int getMinLevel() {
        return this.minLevel;
    }
    
    public int getMaxLevel() {
        return this.maxLevel;
    }
}
