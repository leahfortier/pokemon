package mapMaker.dialogs.action;

import gui.view.ViewMode;
import map.triggers.CommonTrigger;
import mapMaker.dialogs.action.panel.BattleActionPanel;
import mapMaker.dialogs.action.panel.ChoiceActionPanel;
import mapMaker.dialogs.action.panel.EmptyActionPanel;
import mapMaker.dialogs.action.panel.EnumActionPanel;
import mapMaker.dialogs.action.panel.GiveItemActionPanel;
import mapMaker.dialogs.action.panel.MoveNpcActionPanel;
import mapMaker.dialogs.action.panel.PokemonActionPanel;
import mapMaker.dialogs.action.panel.StringActionPanel;
import mapMaker.dialogs.action.panel.TradePokemonActionPanel;
import pattern.action.EmptyActionMatcher;
import pattern.action.EmptyActionMatcher.DayCareActionMatcher;
import pattern.action.EmptyActionMatcher.HealPartyActionMatcher;
import pattern.action.EmptyActionMatcher.ReloadMapActionMatcher;
import pattern.action.EntityActionMatcher.UpdateActionMatcher;
import pattern.action.EnumActionMatcher;
import pattern.action.EnumActionMatcher.BadgeActionMatcher;
import pattern.action.EnumActionMatcher.ChangeViewActionMatcher;
import pattern.action.EnumActionMatcher.CommonTriggerActionMatcher;
import pattern.action.EnumActionMatcher.MedalCountActionMatcher;
import pattern.action.EnumActionMatcher.SoundActionMatcher;
import pattern.action.StringActionMatcher;
import pattern.action.StringActionMatcher.DialogueActionMatcher;
import pattern.action.StringActionMatcher.GlobalActionMatcher;
import pattern.action.StringActionMatcher.MovePlayerActionMatcher;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;

import java.util.function.Function;
import java.util.function.Supplier;

public enum ActionType {
    BADGE("Badge", Badge.values(), BadgeActionMatcher::new),
    BATTLE(BattleActionPanel::new),
    CHANGE_VIEW("View Mode", ViewMode.values(), ChangeViewActionMatcher::new),
    CHOICE(ChoiceActionPanel::new),
    COMMON_TRIGGER("Trigger Name", CommonTrigger.values(), CommonTriggerActionMatcher::new),
    DAY_CARE(DayCareActionMatcher::new),
    DIALOGUE("Dialogue", DialogueActionMatcher::new),
    GIVE_ITEM(dialog -> new GiveItemActionPanel()),
    GIVE_POKEMON(dialog -> new PokemonActionPanel()),
    GLOBAL("Global Name", GlobalActionMatcher::new),
    HEAL_PARTY(HealPartyActionMatcher::new),
    MEDAL_COUNT("Medal", MedalTheme.values(), MedalCountActionMatcher::new),
    MOVE_NPC(dialog -> new MoveNpcActionPanel()),
    MOVE_PLAYER("Player Path", MovePlayerActionMatcher::new),
    RELOAD_MAP(ReloadMapActionMatcher::new),
    SOUND("Sound Title", SoundTitle.values(), SoundActionMatcher::new),
    TRADE_POKEMON(dialog -> new TradePokemonActionPanel()),
    UPDATE("Update Name", UpdateActionMatcher::new);

    private final ActionDataCreator actionDataCreator;

    ActionType(Supplier<EmptyActionMatcher> actionMatcherGetter) {
        this(dialog -> new EmptyActionPanel(actionMatcherGetter));
    }

    ActionType(String stringActionPanelLabel, Function<String, StringActionMatcher> actionMatcherGetter) {
        this(dialog -> new StringActionPanel(stringActionPanelLabel, actionMatcherGetter));
    }

    <T extends Enum> ActionType(String enumTriggerPanelLabel, T[] enumValues, Function<T, EnumActionMatcher<T>> actionMatcherGetter) {
        this(dialog -> new EnumActionPanel<>(enumTriggerPanelLabel, enumValues, actionMatcherGetter));
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
