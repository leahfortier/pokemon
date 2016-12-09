package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import gui.button.Button;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer.Action;

import java.awt.Graphics;
import java.util.List;

public class FightState implements VisualStateHandler {

    private Button[] moveButtons;

    private List<Move> selectedMoveList;

    // The last move that a Pokemon used
    private int lastMoveUsed;

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

        // TODO: I think it would be cool to have the selected move's information on the panel when there isn't a message instead of select a move -- same with bag items
        String message = view.getMessage(VisualState.INVALID_FIGHT, "Select a move!");
        view.drawMenuMessagePanel(g, message);
        view.drawButtonsPanel(g);

        ActivePokemon playerPokemon = Game.getPlayer().front();

        List<Move> moves = playerPokemon.getMoves(view.getCurrentBattle());
        for (int i = 0; i < moves.size(); i++) {
            view.drawMoveButton(g, this.moveButtons[i], moves.get(i));
        }

        view.drawBackButton(g);
    }

    @Override
    public void update(BattleView view) {
        // Update move buttons and the back button
        view.setSelectedButton(moveButtons);

        CharacterData player = Game.getPlayer();
        Battle currentBattle = view.getCurrentBattle();

        // Get the Pokemon that is attacking and their corresponding move list
        ActivePokemon front = player.front();

        for (int i = 0; i < selectedMoveList.size(); i++) {
            if (moveButtons[i].checkConsumePress()) {
                lastMoveUsed = i;

                // Execute the move if valid
                if (Move.validMove(currentBattle, front, selectedMoveList.get(i), true)) {
                    player.performAction(currentBattle, Action.FIGHT);
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
