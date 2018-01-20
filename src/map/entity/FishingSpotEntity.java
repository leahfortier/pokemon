package map.entity;

import map.entity.EntityAction.TriggerAction;
import map.overworld.WildEncounterInfo;
import map.triggers.TriggerType;
import pattern.map.FishingMatcher;
import util.Point;
import util.SerializationUtils;

import java.util.Collections;

public class FishingSpotEntity extends Entity {

    private final WildEncounterInfo[] wildEncounters;
    private boolean dataCreated;

    public FishingSpotEntity(Point location, String entityName, String condition, WildEncounterInfo[] wildEncounters) {
        super(location, entityName, condition);

        this.wildEncounters = wildEncounters;
        this.dataCreated = false;
    }

    @Override
    public boolean isHighPriorityEntity() {
        return false;
    }

    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public void addData() {
        if (dataCreated) {
            return;
        }

        // TODO: condition -- need fishing rod
        FishingMatcher fishingMatcher = new FishingMatcher(this.getEntityName(), wildEncounters);
        EntityAction entityAction = new TriggerAction(TriggerType.FISHING, SerializationUtils.getJson(fishingMatcher), null);

        EntityAction.addActionGroupTrigger(
                this.getEntityName(),
                this.getTriggerSuffix(),
                this.getConditionString(),
                Collections.singletonList(entityAction)
        );

        dataCreated = true;
    }
}
