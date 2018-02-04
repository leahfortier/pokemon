package pattern.generic;

import map.condition.Condition;
import map.condition.ConditionSet;
import pattern.map.ConditionMatcher;

public abstract class TriggerMatcher {
    private String triggerName;
    private String conditionName;
    private ConditionSet condition;

    public String getTriggerName() {
        return this.triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public Condition getCondition() {
        return ConditionMatcher.getCondition(conditionName, condition);
    }

    public void setCondition(String conditionName) {
        this.conditionName = conditionName;
    }

    public void setCondition(Condition condition) {
        this.condition = new ConditionSet(condition);
    }
}
