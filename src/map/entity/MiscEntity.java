package map.entity;

import map.condition.Condition;
import map.triggers.Trigger;
import pattern.action.ActionMatcher;
import util.Point;

import java.util.List;

public class MiscEntity extends Entity {
    private final List<ActionMatcher> actions;

    private Trigger trigger;

    public MiscEntity(String name, Point location, Condition condition, List<ActionMatcher> actions) {
        super(location, name, condition);
        this.actions = actions;
    }

    @Override
    public Trigger getTrigger() {
        if (trigger == null) {
            this.trigger = ActionMatcher.getActionGroupTrigger(
                    this.getEntityName(),
                    this.getCondition(),
                    this.actions
            );
        }

        return trigger;
    }
}
