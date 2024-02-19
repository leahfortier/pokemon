package message;

import gui.view.ViewMode;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import message.Messages.MessageState;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.player.Player;

public enum MessageUpdateType {
    NO_UPDATE,
    TRIGGER,
    RESET_STATE,
    ENTER_BATTLE,
    ENTER_NAME,
    APPEND_TO_NAME,
    SHOW_POKEMON,
    LEARN_MOVE(VisualState.LEARN_MOVE),
    STAT_GAIN(VisualState.STAT_GAIN),
    EXIT_BATTLE(ViewMode.MAP_VIEW),
    CATCH_POKEMON(ViewMode.NEW_POKEMON_VIEW),
    FORCE_SWITCH(battleView -> {
        battleView.setVisualState(VisualState.POKEMON);
        battleView.setSwitchForced();
        battleView.clearUpdate();
    }),
    WIN_BATTLE(battleView -> {
        if (Game.getPlayer().getOptions().shouldPlayBattleMusic()) {
            SoundTitle music = battleView.getCurrentBattle().isWildBattle()
                    ? SoundTitle.WILD_POKEMON_DEFEATED
                    : SoundTitle.TRAINER_DEFEATED; // TODO: Get trainer win music
            SoundPlayer.instance().playMusic(music);
        }
    });

    private final PerformUpdate performUpdate;
    private final boolean exitMessage;

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
        this(false, performUpdate);
    }

    // Exit Battle Messages
    MessageUpdateType(final ViewMode viewMode) {
        this(true, battleView -> {
            Game.instance().setViewMode(viewMode);
            battleView.clearUpdate();
            Messages.clearMessages(MessageState.FIGHTY_FIGHT);
            Messages.setMessageState(MessageState.MAPPITY_MAP);

            Player player = Game.getPlayer();
            player.getEntity().resetCurrentInteractionEntity();
            player.checkEvolution();
            player.exitBattle();
        });
    }

    MessageUpdateType(boolean exitMessage, PerformUpdate performUpdate) {
        this.exitMessage = exitMessage;
        this.performUpdate = performUpdate;
    }

    public void performUpdate(BattleView battleView) {
        this.performUpdate.performUpdate(battleView);
    }

    public boolean isExitMessage() {
        return this.exitMessage;
    }

    @FunctionalInterface
    private interface PerformUpdate {
        void performUpdate(BattleView battleView);
    }
}
