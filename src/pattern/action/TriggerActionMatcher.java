package pattern.action;

import map.triggers.TriggerType;

public class TriggerActionMatcher {
    private String triggerType;
    private String triggerContents;

    public TriggerActionMatcher(TriggerType triggerType, String triggerContents) {
        this.triggerType = triggerType.name();
        this.triggerContents = triggerContents;
    }

    public TriggerType getTriggerType() {
        return TriggerType.getTriggerType(this.triggerType);
    }

    public String getTriggerContents() {
        return this.triggerContents;
    }
}
