package map.entity;

import map.condition.Condition;
import map.triggers.Trigger;
import pattern.action.ActionList;
import util.Point;

public class MiscEntity extends Entity {
    private final ActionList actions;

    private Trigger trigger;

    public MiscEntity(String name, Point location, Condition condition, ActionList actions) {
        super(location, name, condition);
        this.actions = actions;
    }

    @Override
    public Trigger getTrigger() {
        if (trigger == null) {
            this.trigger = this.actions.getGroupTrigger(
                    this.getEntityName(),
                    this.getCondition()
            );
        }

        return trigger;
    }
}
