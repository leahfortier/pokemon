package pattern.map;

import map.entity.Entity;
import map.entity.FishingSpotEntity;
import map.overworld.wild.WildEncounterInfo;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.EntityMatcher.MultiEntityMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import util.Point;

import java.util.List;

public class FishingMatcher extends MultiPointTriggerMatcher implements MultiEntityMatcher {
    private String name;
    private WildEncounterInfo[] wildPokemon;

    public FishingMatcher(String name, List<WildEncounterInfo> wildEncounters) {
        this.name = name;
        this.wildPokemon = wildEncounters.toArray(new WildEncounterInfo[0]);
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

    @Override
    public Entity createEntity(Point location) {
        return new FishingSpotEntity(
                location,
                this.getTriggerName(),
                this.getCondition(),
                this.getWildEncounters()
        );
    }
}
