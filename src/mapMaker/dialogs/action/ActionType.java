package mapMaker.dialogs.action;

public enum ActionType {
    TRIGGER(TriggerActionPanel::new),
    BATTLE(BattleActionPanel::new),
    CHOICE(ChoiceActionPanel::new),
    UPDATE(() -> new BasicActionPanel("Update Name")),
    GROUP_TRIGGER(() -> new BasicActionPanel("Trigger Name")),
    GLOBAL(() -> new BasicActionPanel("Global Name"));

    private final ActionDataCreator actionDataCreator;

    ActionType(ActionDataCreator actionDataCreator) {
        this.actionDataCreator = actionDataCreator;
    }

    private interface ActionDataCreator {
        ActionPanel createData();
    }

    public ActionPanel createActionData() {
        return this.actionDataCreator.createData();
    }
}
