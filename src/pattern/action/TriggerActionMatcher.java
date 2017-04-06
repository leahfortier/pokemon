package pattern.action;

import map.triggers.TriggerType;
import mapMaker.dialogs.action.trigger.TriggerActionType;
import util.StringUtils;

public class TriggerActionMatcher {
    private String triggerType;
    private String triggerContents;

    public TriggerActionMatcher(TriggerActionType triggerType, String triggerContents) {
        this.triggerType = triggerType.name();
        this.triggerContents = triggerContents;
    }

    public TriggerType getTriggerType() {
        return this.getTriggerActionType().getTriggerType();
    }

    public TriggerActionType getTriggerActionType() {
        return TriggerActionType.valueOf(StringUtils.getNamesiesString(triggerType));
    }

    public String getTriggerContents() {
        return this.triggerContents;
    }
}
