package mapMaker.dialogs.action;

import mapMaker.dialogs.action.trigger.TriggerActionPanel;

public enum ActionType {
    TRIGGER(TriggerActionPanel::new),
    BATTLE(BattleActionPanel::new),
    CHOICE(ChoiceActionPanel::new),
    UPDATE(dialog -> new BasicActionPanel("Update Name")),
    GROUP_TRIGGER(dialog -> new BasicActionPanel("Trigger Name")),
    GLOBAL(dialog -> new BasicActionPanel("Global Name"));

    private final ActionDataCreator actionDataCreator;

    ActionType(ActionDataCreator actionDataCreator) {
        this.actionDataCreator = actionDataCreator;
    }

    private interface ActionDataCreator {
        ActionPanel createData(ActionDialog actionDialog);
    }

    public ActionPanel createActionData(ActionDialog actionDialog) {
        return this.actionDataCreator.createData(actionDialog);
    }
}
