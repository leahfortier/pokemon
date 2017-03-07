package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import draw.button.Button;
import draw.button.panel.DrawPanel;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import pokemon.ActivePokemon;
import trainer.TrainerAction;
import trainer.player.Player;
import util.StringUtils;

import java.awt.Graphics;
import java.util.List;

public class FightState implements VisualStateHandler {
    private final DrawPanel moveDetailsPanel;

    private Button[] moveButtons;

    private List<Move> selectedMoveList;

    // The last move that a Pokemon used
    private int lastMoveUsed;

    public FightState() {
        moveDetailsPanel = new DrawPanel(415, 440, 385, 161)
                .withBorderPercentage(8)
                .withBlackOutline()
                .withTransparentCount(2);
    }

    @Override
    public void reset() {
        this.resetLastMoveUsed();
    }

    @Override
    public void set(BattleView view) {
        moveButtons = view.createPanelButtons();

        view.setSelectedButton(lastMoveUsed);
        selectedMoveList = Game.getPlayer().front().getMoves(view.getCurrentBattle());
        for (int i = 0; i < Move.MAX_MOVES; i++) {
            moveButtons[i].setActive(i < selectedMoveList.size());
        }

        for (Button button : moveButtons) {
            button.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawButtonsPanel(g);

        ActivePokemon playerPokemon = Game.getPlayer().front();
        List<Move> moves = playerPokemon.getMoves(view.getCurrentBattle());
        for (int i = 0; i < moves.size(); i++) {
            this.moveButtons[i].drawMoveButton(g, moves.get(i));
        }

        String message = view.getMessage(VisualState.INVALID_FIGHT, null);
        if (StringUtils.isNullOrEmpty(message)) {
            // Draw move details
            moveDetailsPanel.drawMovePanel(g, moves.get(view.getSelectedButton()).getAttack());
        }
        else {
            // Show unusable move message
            view.drawMenuMessagePanel(g, message);
        }
    }

    @Override
    public void update(BattleView view) {
        // Update move buttons and the back button
        view.setSelectedButton(moveButtons);

        Player player = Game.getPlayer();
        Battle currentBattle = view.getCurrentBattle();

        // Get the Pokemon that is attacking and their corresponding move list
        ActivePokemon front = player.front();

        for (int i = 0; i < selectedMoveList.size(); i++) {
            if (moveButtons[i].checkConsumePress()) {
                lastMoveUsed = i;

                // Execute the move if valid
                if (Move.validMove(currentBattle, front, selectedMoveList.get(i), true)) {
                    player.performAction(currentBattle, TrainerAction.FIGHT);
                    view.setVisualState(VisualState.MESSAGE);
                    view.cycleMessage(false);
                }
                // An invalid move -- Don't let them select it
                else {
                    view.cycleMessage(false);
                    view.setVisualState(VisualState.INVALID_FIGHT);
                }
            }
        }

        // Return to main battle menu
        view.updateBackButton();
    }

    public void resetLastMoveUsed() {
        this.lastMoveUsed = 0;
    }
}
