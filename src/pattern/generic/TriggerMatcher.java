package pattern.generic;

import util.StringUtils;

public abstract class TriggerMatcher {
    private String triggerName;
    private String condition;

    public String getTriggerName() {
        return this.triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = StringUtils.nullWhiteSpace(condition);
    }
}
