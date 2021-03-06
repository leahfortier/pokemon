package map.entity;

import item.ItemNamesies;
import map.condition.Condition;
import map.condition.Condition.ItemCondition;
import map.condition.ConditionHolder.AndCondition;
import map.overworld.wild.WildEncounterInfo;
import map.triggers.Trigger;
import map.triggers.battle.FishingTrigger;
import util.Point;

public class FishingSpotEntity extends Entity {
    private final WildEncounterInfo[] wildEncounters;
    private Trigger trigger;

    public FishingSpotEntity(Point location, String entityName, Condition condition, WildEncounterInfo[] wildEncounters) {
        super(location, entityName, new AndCondition(condition, new ItemCondition(ItemNamesies.FISHING_ROD)));

        this.wildEncounters = wildEncounters;
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
    public Trigger getTrigger() {
        if (trigger == null) {
            this.trigger = new FishingTrigger(wildEncounters);
        }

        return trigger;
    }
}
