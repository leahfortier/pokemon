package pattern.action;

import map.triggers.TriggerType;

public class TriggerActionMatcher {
    private String triggerType;
    public String triggerContents;

    public TriggerActionMatcher(TriggerType triggerType, String triggerContents) {
        this.triggerType = triggerType.name();
        this.triggerContents = triggerContents;
    }

    public TriggerType getTriggerType() {
        return TriggerType.getTriggerType(this.triggerType);
    }
}
