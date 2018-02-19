package mapMaker.dialogs.action;

import mapMaker.dialogs.action.trigger.TriggerActionPanel;

public enum ActionType {
    // Custom Panels
    TRIGGER(TriggerActionPanel::new),
    BATTLE(BattleActionPanel::new),
    CHOICE(ChoiceActionPanel::new),

    // TODO: Custom
    GIVE_POKEMON(dialog -> new EmptyActionPanel()),
    TRADE_POKEMON(dialog -> new EmptyActionPanel()),
    MOVE_NPC(dialog -> new EmptyActionPanel()),
    FISHING(dialog -> new EmptyActionPanel()),

    // Empty Panels
    HEAL_PARTY(dialog -> new EmptyActionPanel()),
    DAY_CARE(dialog -> new EmptyActionPanel()),
    RELOAD_MAP(dialog -> new EmptyActionPanel()),

    // TODO: Item Panels
    USE_ITEM(dialog -> new StringActionPanel("Item Name")),
    GIVE_ITEM(dialog -> new StringActionPanel("Item Name")),

    // TODO: Enum Panels
    BADGE(dialog -> new StringActionPanel("Badge Name")),
    CHANGE_VIEW(dialog -> new StringActionPanel("Change View")),
    SOUND(dialog -> new StringActionPanel("Sound Title")),
    MEDAL_COUNT(dialog -> new StringActionPanel("Medal Count")),

    // String Panels
    DIALOGUE(dialog -> new StringActionPanel("Dialogue")),
    UPDATE(dialog -> new StringActionPanel("Update Name")),
    GROUP_TRIGGER(dialog -> new StringActionPanel("Trigger Name")),
    GLOBAL(dialog -> new StringActionPanel("Global Name")),
    MOVE_PLAYER(dialog -> new StringActionPanel("Player Path"));

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
