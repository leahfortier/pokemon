package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import message.MessageUpdate;
import message.Messages;

import java.awt.Color;
import java.awt.Graphics;

public class LearnMoveQuestionState implements VisualStateHandler {

    private static final int NUM_COLS = 4;

    private Button[] buttons;

    @Override
    public void set(BattleView view) {
        buttons = view.createMessagePanelButtons(2, NUM_COLS);

        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];

            button.setActive(button == yesButton() || button == noButton());

            if (button == yesButton()) {
                view.setSelectedButton(i);
            }
        }
    }

    // Bottom center left
    private Button yesButton() {
        return buttons[NUM_COLS + 1];
    }

    // Bottom center right
    private Button noButton() {
        return buttons[NUM_COLS + 2];
    }

    private void drawButton(Graphics g, Button button, Color color, String label) {
        button.fillBordered(g, color);
        button.blackOutline(g);
        button.label(g, 30, label);
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        view.drawFullMessagePanel(g, "Delete a move in order to learn " + view.getLearnedMove().getAttack().getName() + "?");

        drawButton(g, yesButton(), new Color(120, 200, 80), "Yes");
        drawButton(g, noButton(), new Color(220, 20, 20), "No");

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    @Override
    public void update(BattleView view) {
        view.setSelectedButton(buttons);

        if (noButton().checkConsumePress()) {

            // This is all done really silly, so we need to do this
            MessageUpdate message = Messages.getNextMessage();
            for (int i = 0; i < Move.MAX_MOVES + 1; i++) {
                Messages.getNextMessage();
            }

            Messages.addMessage(message);

            view.setVisualState(VisualState.MESSAGE);
            view.cycleMessage(false);
        }

        if (yesButton().checkConsumePress()) {
            view.setVisualState(VisualState.LEARN_MOVE_DELETE);
        }
    }
}
