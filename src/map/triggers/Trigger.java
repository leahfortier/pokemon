package map.triggers;

import map.condition.Condition;
import map.condition.ConditionSet;

public abstract class Trigger {
    private final ConditionSet condition;

    protected Trigger() {
        this(null);
    }

    protected Trigger(Condition condition) {
        this.condition = new ConditionSet(condition);
    }

    public abstract void execute();

    // Evaluate the condition, should only be triggered when a player moves
    // into a map square that is defined to trigger this event
    public boolean canTrigger() {
        return condition.evaluate();
    }
}
