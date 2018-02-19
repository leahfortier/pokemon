package mapMaker.dialogs.action;

import gui.view.ViewMode;
import mapMaker.dialogs.action.panel.BattleActionPanel;
import mapMaker.dialogs.action.panel.ChoiceActionPanel;
import mapMaker.dialogs.action.panel.EmptyActionPanel;
import mapMaker.dialogs.action.panel.EnumActionPanel;
import mapMaker.dialogs.action.panel.GiveItemActionPanel;
import mapMaker.dialogs.action.panel.MoveNpcActionPanel;
import mapMaker.dialogs.action.panel.PokemonActionPanel;
import mapMaker.dialogs.action.panel.StringActionPanel;
import mapMaker.dialogs.action.panel.StringActionPanel.ItemActionPanel;
import mapMaker.dialogs.action.panel.TradePokemonActionPanel;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;

public enum ActionType {
    BADGE("Badge", Badge.values()),
    BATTLE(BattleActionPanel::new),
    CHANGE_VIEW("View Mode", ViewMode.values()),
    CHOICE(ChoiceActionPanel::new),
    DAY_CARE(dialog -> new EmptyActionPanel()),
    DIALOGUE("Dialogue"),
    GIVE_ITEM(dialog -> new GiveItemActionPanel()),
    GIVE_POKEMON(dialog -> new PokemonActionPanel()),
    GLOBAL("Global Name"),
    GROUP_TRIGGER("Trigger Name"),
    HEAL_PARTY(dialog -> new EmptyActionPanel()),
    MEDAL_COUNT("Medal", MedalTheme.values()),
    MOVE_NPC(dialog -> new MoveNpcActionPanel()),
    MOVE_PLAYER("Player Path"),
    RELOAD_MAP(dialog -> new EmptyActionPanel()),
    SOUND("Sound Title", SoundTitle.values()),
    TRADE_POKEMON(dialog -> new TradePokemonActionPanel()),
    UPDATE("Update Name"),
    USE_ITEM(dialog -> new ItemActionPanel()),

    // TODO: This should be removed and so should the FishingActionMatcher
    FISHING(dialog -> new EmptyActionPanel());

    private final ActionDataCreator actionDataCreator;

    ActionType(String stringActionPanelLabel) {
        this(dialog -> new StringActionPanel(stringActionPanelLabel));
    }

    <T extends Enum> ActionType(String enumTriggerPanelLabel, T[] enumValues) {
        this(dialog -> new EnumActionPanel<>(enumTriggerPanelLabel, enumValues));
    }

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
