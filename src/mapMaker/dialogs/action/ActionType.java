package mapMaker.dialogs.action;

import mapMaker.dialogs.action.trigger.TriggerActionPanel;

public enum ActionType {
    TRIGGER(TriggerActionPanel::new),
    BATTLE(BattleActionPanel::new),
    CHOICE(ChoiceActionPanel::new),
    USE_ITEM(dialog -> new BasicActionPanel("Item Name")), // TODO: New item thingy
    DIALOGUE(dialog -> new BasicActionPanel("Dialogue")),
    UPDATE(dialog -> new BasicActionPanel("Update Name")),
    GROUP_TRIGGER(dialog -> new BasicActionPanel("Trigger Name")),
    GLOBAL(dialog -> new BasicActionPanel("Global Name"));

    private final ActionDataCreator actionDataCreator;

    ActionType(ActionDataCreator actionDataCreator) {
        this.actionDataCreator = actionDataCreator;
    }

    public ActionPanel createActionData(ActionDialog actionDialog) {
        return this.actionDataCreator.createData(actionDialog);
    }

    @FunctionalInterface
    private interface ActionDataCreator {
        ActionPanel createData(ActionDialog actionDialog);
    }
}
