package mapMaker.dialogs.action.trigger;

import gui.view.ViewMode;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.trigger.StringTriggerPanel.ItemTriggerPanel;
import mapMaker.dialogs.action.trigger.TriggerContentsPanel.EmptyTriggerContentsPanel;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;

import java.util.function.Supplier;

public enum TriggerActionType {
    BADGE(TriggerType.BADGE, "Badge", Badge.values()),
    CHANGE_VIEW(TriggerType.CHANGE_VIEW, "View Mode", ViewMode.values()),
    DAY_CARE(TriggerType.DAY_CARE, EmptyTriggerContentsPanel::new),
    DIALOGUE(TriggerType.DIALOGUE, "Dialogue"),
    GIVE_ITEM(TriggerType.GIVE_ITEM, ItemTriggerPanel::new),
    GIVE_POKEMON(TriggerType.GIVE_POKEMON, PokemonTriggerPanel::new),
    GLOBAL(TriggerType.GLOBAL, "Global Name"),
    GROUP(TriggerType.GROUP, "Group Trigger Name"),
    HEAL_PARTY(TriggerType.HEAL_PARTY, EmptyTriggerContentsPanel::new),
    MEDAL_COUNT(TriggerType.MEDAL_COUNT, "Medal", MedalTheme.values()),
    MOVE_NPC(TriggerType.MOVE_NPC, MoveNPCTriggerPanel::new),
    MOVE_PLAYER(TriggerType.MOVE_PLAYER, "Player Move Path"),
    RELOAD_MAP(TriggerType.RELOAD_MAP, EmptyTriggerContentsPanel::new),
    SOUND(TriggerType.SOUND, "Sound Title", SoundTitle.values()),
    TRADE_POKEMON(TriggerType.TRADE_POKEMON, TradePokemonTriggerPanel::new),
    USE_ITEM(TriggerType.USE_ITEM, ItemTriggerPanel::new);

    private final TriggerType triggerType;
    private final Supplier<TriggerContentsPanel> panelCreator;

    TriggerActionType(TriggerType triggerType, String stringTriggerPanelLabel) {
        this(triggerType, () -> new StringTriggerPanel(stringTriggerPanelLabel));
    }

    <T extends Enum> TriggerActionType(TriggerType triggerType, String enumTriggerPanelLabel, T[] enumValues) {
        this(triggerType, () -> new EnumTriggerPanel<>(enumTriggerPanelLabel, enumValues));
    }

    TriggerActionType(TriggerType triggerType, Supplier<TriggerContentsPanel> panelCreator) {
        this.triggerType = triggerType;
        this.panelCreator = panelCreator;
    }

    public TriggerContentsPanel createPanel() {
        return this.panelCreator.get();
    }

    public TriggerType getTriggerType() {
        return this.triggerType;
    }
}
