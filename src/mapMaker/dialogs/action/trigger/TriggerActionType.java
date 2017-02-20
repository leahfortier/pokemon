package mapMaker.dialogs.action.trigger;

import gui.view.ViewMode;
import map.triggers.TriggerType;
import mapMaker.dialogs.action.trigger.StringTriggerPanel.ItemTriggerPanel;
import mapMaker.dialogs.action.trigger.StringTriggerPanel.MovePlayerTriggerPanel;
import mapMaker.dialogs.action.trigger.TriggerContentsPanel.EmptyTriggerContentsPanel;
import sound.SoundTitle;
import trainer.Badge;

public enum TriggerActionType {
    BADGE(TriggerType.BADGE, () -> new EnumTriggerPanel<>("Badge", Badge.values())),
    CHANGE_VIEW(TriggerType.CHANGE_VIEW, () -> new EnumTriggerPanel<>("View Mode", ViewMode.values())),
    DIALOGUE(TriggerType.DIALOGUE, () -> new StringTriggerPanel("Dialogue")),
    GIVE_ITEM(TriggerType.GIVE_ITEM, ItemTriggerPanel::new),
    GIVE_POKEMON(TriggerType.GIVE_POKEMON, PokemonTriggerPanel::new),
    GLOBAL(TriggerType.GLOBAL, () -> new StringTriggerPanel("Global Name")),
    GROUP(TriggerType.GROUP, () -> new StringTriggerPanel("Group Trigger Name")),
    HEAL_PARTY(TriggerType.HEAL_PARTY, EmptyTriggerContentsPanel::new),
    MOVE_NPC(TriggerType.MOVE_NPC, MoveNPCTriggerPanel::new),
    MOVE_PLAYER(TriggerType.MOVE_PLAYER, MovePlayerTriggerPanel::new),
    SOUND(TriggerType.SOUND, () -> new EnumTriggerPanel<>("Sound Title", SoundTitle.values()));

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
