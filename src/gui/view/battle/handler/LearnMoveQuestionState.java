package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import message.MessageUpdate;
import message.Messages;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public class LearnMoveQuestionState implements VisualStateHandler {

    private final Button yesButton;
    private final Button noButton;

    public LearnMoveQuestionState() {
        // Learn Move Buttons
//        yesButton = new Button(moveButtons[2].x, moveButtons[2].y, moveButtons[2].width, moveButtons[2].height, Button.HoverAction.BOX);
//        noButton = new Button(moveButtons[3].x, moveButtons[3].y, moveButtons[3].width, moveButtons[3].height, Button.HoverAction.BOX);
        yesButton = new Button(0, 0, 0, 0, Button.HoverAction.BOX); // TODO
        noButton = new Button(0, 0, 0, 0, Button.HoverAction.BOX); // TODO
    }

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);
        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 25);
        g.drawString("Delete a move in order to learn " + VisualState.getLearnedMove().getAttack().getName() + "?", 30, 490);

        g.translate(yesButton.x, yesButton.y);

        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 183, 55);
        g.drawImage(tiles.getTile(0x22), 0, 0, null);

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 22);
        g.drawString("Yes", 10, 26);

        g.translate(-yesButton.x, -yesButton.y);

        g.translate(noButton.x, noButton.y);

        g.setColor(Color.RED);
        g.fillRect(0, 0, 183, 55);
        g.drawImage(tiles.getTile(0x22), 0, 0, null);

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 22);
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
