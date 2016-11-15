package pattern.generic;

import util.StringUtils;

public abstract class TriggerMatcher {
    public String triggerName;
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
    protected void setCondition(String condition) {
        this.condition = StringUtils.nullWhiteSpace(condition);
    }
}
