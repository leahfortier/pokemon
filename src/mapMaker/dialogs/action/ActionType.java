package mapMaker.dialogs.action;

public enum ActionType {
    TRIGGER(TriggerActionPanel::new),
    BATTLE(BattleActionPanel::new),
    CHOICE(ChoiceActionPanel::new),
    UPDATE(BasicActionPanel::new),
    GROUP_TRIGGER(BasicActionPanel::new),
    GLOBAL(BasicActionPanel::new);

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
