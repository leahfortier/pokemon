package map.entity;

import map.condition.Condition;
import pattern.action.ActionMatcher;
import util.Point;

import java.util.List;

public class MiscEntity extends Entity {
    private final List<ActionMatcher> actions;

    private boolean dataCreated;

    public MiscEntity(String name, Point location, Condition condition, List<ActionMatcher> actions) {
        super(location, name, condition);
        this.actions = actions;
        this.dataCreated = false;
    }

    @Override
    public void addData() {
        if (dataCreated) {
            return;
        }

        ActionMatcher.addActionGroupTrigger(this.getEntityName(), this.getTriggerSuffix(), this.getCondition(), this.actions);
        dataCreated = true;
    }
}
