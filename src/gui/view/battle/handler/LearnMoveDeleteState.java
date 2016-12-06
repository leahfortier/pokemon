package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import message.MessageUpdate;
import message.Messages;
import util.DrawUtils;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class LearnMoveDeleteState implements VisualStateHandler {

    private static final int NUM_COLS = 4;

    private Button[] buttons;

    @Override
    public void set(BattleView view) {
        // Create a button for each known move and then one for the new move and one for not learning
        buttons = view.createMessagePanelButtons(2, NUM_COLS);

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < NUM_COLS; x++) {
                int index = Point.getIndex(x, y, NUM_COLS);
                buttons[index].setActive(x < Move.MAX_MOVES/2 || buttons[index] == newMoveButton());
            }
        }

        view.setSelectedButton(0);
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawFullMessagePanel(g, StringUtils.empty());

        List<Move> moves = view.getLearnedPokemon().getActualMoves();
        for (int y = 0, moveIndex = 0; y < 2; y++) {
            for (int x = 0; x < Move.MAX_MOVES/2; x++, moveIndex++) {
                int index = Point.getIndex(x, y, NUM_COLS);
                view.drawMoveButton(g, buttons[index], moves.get(moveIndex));
            }
        }

        Button newMoveButton = newMoveButton();
        view.drawMoveButton(g, newMoveButton, view.getLearnedMove());

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 25);
        int centerX = (buttons[NUM_COLS - 2].centerX() + buttons[NUM_COLS - 1].centerX())/2;
        DrawUtils.drawCenteredString(g, "Select a move to delete!", centerX, buttons[0].centerY());

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    @Override
    public void update(BattleView view) {
        view.setSelectedButton(buttons);

        for (int y = 0, moveIndex = 0; y < 2; y++) {
            for (int x = 0; x < Move.MAX_MOVES/2; x++, moveIndex++) {
                int index = Point.getIndex(x, y, NUM_COLS);
                if (buttons[index].checkConsumePress()) {
                    // TODO: To fix that awful shitty shit see if we can utilize to new add to front method
                    view.getLearnedPokemon().addMove(view.getCurrentBattle(), view.getLearnedMove(), moveIndex);

                    // This is all done really silly, so we need to do this
                    MessageUpdate message = Messages.getNextMessage();
                    for (int j = 0; j < Move.MAX_MOVES; j++) {
                        if (j == moveIndex) {
                            message = Messages.getNextMessage();
                        }
                        else {
                            Messages.getNextMessage();
                        }
                    }

                    Messages.addMessage(message);

                    view.setVisualState(VisualState.MESSAGE);
                    view.cycleMessage(false);
                }
            }
        }

        if (newMoveButton().checkConsumePress()) {
            // This is all done really silly, so we need to do this
            MessageUpdate message = Messages.getNextMessage();
            for (int i = 0; i < Move.MAX_MOVES + 1; i++) {
                Messages.getNextMessage();
            }

            Messages.addMessage(message);

            view.setVisualState(VisualState.MESSAGE);
            view.cycleMessage(false);
        }
    }

    private Button newMoveButton() {
        return this.buttons[buttons.length - 2];
    }
}
