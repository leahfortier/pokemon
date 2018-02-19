package mapMaker.dialogs.action.trigger;

import gui.view.ViewMode;
import mapMaker.dialogs.action.trigger.StringTriggerPanel.ItemTriggerPanel;
import mapMaker.dialogs.action.trigger.TriggerContentsPanel.EmptyTriggerContentsPanel;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;

import java.util.function.Supplier;

public enum TriggerActionType {
    BADGE("Badge", Badge.values()),
    CHANGE_VIEW("View Mode", ViewMode.values()),
    DAY_CARE(EmptyTriggerContentsPanel::new),
    DIALOGUE("Dialogue"),
    GIVE_ITEM(ItemTriggerPanel::new),
    GIVE_POKEMON(PokemonTriggerPanel::new),
    GROUP("Group Trigger Name"),
    HEAL_PARTY(EmptyTriggerContentsPanel::new),
    MEDAL_COUNT("Medal", MedalTheme.values()),
    MOVE_NPC(MoveNPCTriggerPanel::new),
    MOVE_PLAYER("Player Move Path"),
    RELOAD_MAP(EmptyTriggerContentsPanel::new),
    SOUND("Sound Title", SoundTitle.values()),
    TRADE_POKEMON(TradePokemonTriggerPanel::new),
    USE_ITEM(ItemTriggerPanel::new);

    private final Supplier<TriggerContentsPanel> panelCreator;

    TriggerActionType(String stringTriggerPanelLabel) {
        this(() -> new StringTriggerPanel(stringTriggerPanelLabel));
    }

    <T extends Enum> TriggerActionType(String enumTriggerPanelLabel, T[] enumValues) {
        this(() -> new EnumTriggerPanel<>(enumTriggerPanelLabel, enumValues));
    }

    TriggerActionType(Supplier<TriggerContentsPanel> panelCreator) {
        this.panelCreator = panelCreator;
    }

    public TriggerContentsPanel createPanel() {
        return this.panelCreator.get();
    }
}
