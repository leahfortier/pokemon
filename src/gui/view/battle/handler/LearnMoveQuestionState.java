package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import message.MessageUpdate;
import message.Messages;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class LearnMoveQuestionState implements VisualStateHandler {

    private final Button yesButton;
    private final Button noButton;

    public LearnMoveQuestionState() {
        yesButton = BattleView.createSubMenuButton(0, 1, null);
        noButton = BattleView.createSubMenuButton(1, 1, null);
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);
        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 25);
        g.drawString("Delete a move in order to learn " + view.getLearnedMove().getAttack().getName() + "?", 30, 490);

        g.translate(yesButton.x, yesButton.y);

        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 183, 55);
        g.drawImage(tiles.getTile(0x22), 0, 0, null);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 22);
        g.drawString("Yes", 10, 26);

        g.translate(-yesButton.x, -yesButton.y);

        g.translate(noButton.x, noButton.y);

        g.setColor(Color.RED);
        g.fillRect(0, 0, 183, 55);
        g.drawImage(tiles.getTile(0x22), 0, 0, null);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 22);
        g.drawString("No", 10, 26);

        g.translate(-noButton.x, -noButton.y);

        yesButton.draw(g);
        noButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        yesButton.update();
        noButton.update();

        if (noButton.checkConsumePress()) {

            // This is all done really silly, so we need to do this
            MessageUpdate message = Messages.getNextMessage();
            for (int i = 0; i < Move.MAX_MOVES + 1; i++) {
                Messages.getNextMessage();
            }

            Messages.addMessage(message);

            view.setVisualState(VisualState.MESSAGE);
            view.cycleMessage(false);
        }

        if (yesButton.checkConsumePress()) {
            view.setVisualState(VisualState.LEARN_MOVE_DELETE);
        }
    }
}
