package mapMaker.dialogs.action.trigger;

import gui.view.ViewMode;
import map.triggers.TriggerType;
import sound.SoundTitle;

public enum TriggerActionType {
    DIALOGUE(TriggerType.DIALOGUE, () -> new StringTriggerAction("Dialogue")),
    GLOBAL(TriggerType.GLOBAL, () -> new StringTriggerAction("Global Name")),
    GROUP(TriggerType.GROUP, () -> new StringTriggerAction("Group Trigger Name")),
    CHANGE_VIEW(TriggerType.CHANGE_VIEW, () -> new EnumTriggerAction<>("View Mode", ViewMode.values())),
    SOUND(TriggerType.SOUND, () -> new EnumTriggerAction<>("Sound Title", SoundTitle.values()));

    private final TriggerType triggerType;
    private final TriggerPanelCreator panelCreator;

    TriggerActionType(TriggerType triggerType, TriggerPanelCreator panelCreator) {
        this.triggerType = triggerType;
        this.panelCreator = panelCreator;
    }

    private interface TriggerPanelCreator {
        TriggerContentsPanel createPanel();
    }

    public TriggerContentsPanel createPanel() {
        return this.panelCreator.createPanel();
    }

    public TriggerType getTriggerType() {
        return this.triggerType;
    }
}
