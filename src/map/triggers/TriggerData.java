package map.triggers;

import map.condition.Condition;
import map.condition.ConditionSet;
import map.entity.EntityAction;
import pattern.map.EventMatcher;

import java.util.List;

public class TriggerData {
    private String name;
    private ConditionSet condition;
    private List<EntityAction> actions;

    public TriggerData(EventMatcher matcher) {
        this.name = matcher.getTriggerName();
        this.condition = new ConditionSet(matcher.getCondition());
        this.actions = matcher.getActions();
    }

    public String getName() {
        return this.name;
    }

    public Condition getCondition() {
        return this.condition.getCondition();
    }

    public List<EntityAction> getActions() {
        return this.actions;
    }
}
