package pattern.generic;

import map.condition.Condition;
import map.condition.ConditionSet;

public abstract class TriggerMatcher {
    private String triggerName;
    private ConditionSet condition;

    public String getTriggerName() {
        return this.triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public Condition getCondition() {
        if (this.condition == null) {
            return null;
        }

        return this.condition.getCondition();
    }

    public void setCondition(Condition condition) {
        this.condition = new ConditionSet(condition);
    }
}
