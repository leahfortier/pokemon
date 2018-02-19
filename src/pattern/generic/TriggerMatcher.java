package pattern.generic;

import map.condition.Condition;
import map.condition.ConditionSet;
import pattern.JsonMatcher;
import pattern.map.ConditionMatcher;

public abstract class TriggerMatcher implements JsonMatcher {
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

    public String getConditionName() {
        return conditionName;
    }

    public ConditionSet getConditionSet() {
        return condition;
    }

    public void setCondition(String conditionName, ConditionSet conditionSet) {
        this.conditionName = conditionName;
        this.condition = conditionSet;
    }
}
