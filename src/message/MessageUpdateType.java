package message;

import gui.view.ViewMode;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import message.Messages.MessageState;
import sound.SoundPlayer;
import sound.SoundTitle;

public enum MessageUpdateType {
    NO_UPDATE,
    TRIGGER,
    RESET_STATE,
    ENTER_BATTLE,
    ENTER_NAME,
    APPEND_TO_NAME,
    SHOW_POKEMON,
    PROMPT_SWITCH(VisualState.POKEMON),
    LEARN_MOVE(VisualState.LEARN_MOVE),
    STAT_GAIN(VisualState.STAT_GAIN),
    EXIT_BATTLE(battleView -> exitBattle(battleView, ViewMode.MAP_VIEW)),
    CATCH_POKEMON(battleView -> exitBattle(battleView, ViewMode.NEW_POKEMON_VIEW)),
    FORCE_SWITCH(battleView -> {
        battleView.setVisualState(VisualState.POKEMON);
        battleView.setSwitchForced();
        battleView.clearUpdate();
    }),
    WIN_BATTLE(battleView -> {
        SoundTitle music = battleView.getCurrentBattle().isWildBattle()
                ? SoundTitle.WILD_POKEMON_DEFEATED
                : SoundTitle.TRAINER_DEFEATED; // TODO: Get trainer win music
        SoundPlayer.instance().playMusic(music);
    });

    private final PerformUpdate performUpdate;

    MessageUpdateType() {
        this(battleView -> {});
    }

    MessageUpdateType(final VisualState visualState) {
        this(battleView -> {
            battleView.setVisualState(visualState);
            battleView.clearUpdate();
        });
    }

    MessageUpdateType(PerformUpdate performUpdate) {
        this.performUpdate = performUpdate;
    }

    public void performUpdate(BattleView battleView) {
        this.performUpdate.performUpdate(battleView);
    }

    private static void exitBattle(BattleView battleView, ViewMode viewMode) {
        Game.instance().setViewMode(viewMode);
        battleView.clearUpdate();
        Messages.clearMessages(MessageState.FIGHTY_FIGHT);
        Messages.setMessageState(MessageState.MAPPITY_MAP);
        Game.getPlayer().getEntity().resetCurrentInteractionEntity();

        Game.getPlayer().checkEvolution();
        Game.getPlayer().exitBattle();
    }

    @FunctionalInterface
    private interface PerformUpdate {
        void performUpdate(BattleView battleView);
    }
}
