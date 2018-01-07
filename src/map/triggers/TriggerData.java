package map.triggers;

import map.entity.EntityAction;
import pattern.map.EventMatcher;

import java.util.List;

public class TriggerData {
    private String name;
    private String condition;
    private List<EntityAction> actions;

    public TriggerData(EventMatcher matcher) {
        this.name = matcher.getTriggerName();
        this.condition = matcher.getCondition();
        this.actions = matcher.getActions();
    }

    public String getName() {
        return this.name;
    }

    public String getCondition() {
        return this.condition;
    }

    public List<EntityAction> getActions() {
        return this.actions;
    }
}
