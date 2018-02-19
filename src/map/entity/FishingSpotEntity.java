package map.entity;

import item.ItemNamesies;
import map.condition.Condition;
import map.condition.Condition.ItemCondition;
import map.condition.ConditionHolder.AndCondition;
import map.entity.EntityAction.TriggerAction;
import map.overworld.WildEncounterInfo;
import map.triggers.TriggerType;
import pattern.map.FishingMatcher;
import util.Point;

import java.util.Collections;

public class FishingSpotEntity extends Entity {
    private final WildEncounterInfo[] wildEncounters;
    private boolean dataCreated;

    public FishingSpotEntity(Point location, String entityName, Condition condition, WildEncounterInfo[] wildEncounters) {
        super(location, entityName, new AndCondition(condition, new ItemCondition(ItemNamesies.FISHING_ROD)));

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

        FishingMatcher fishingMatcher = new FishingMatcher(this.getEntityName(), wildEncounters);
        EntityAction entityAction = new TriggerAction(
                TriggerType.FISHING,
                fishingMatcher.getJson(),
                new ItemCondition(ItemNamesies.FISHING_ROD)
        );

        EntityAction.addActionGroupTrigger(
                this.getEntityName(),
                this.getTriggerSuffix(),
                this.getCondition(),
                Collections.singletonList(entityAction)
        );

        dataCreated = true;
    }
}
