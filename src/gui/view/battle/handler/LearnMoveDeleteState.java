package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import message.MessageUpdate;
import message.Messages;
import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class LearnMoveDeleteState implements VisualStateHandler {

    private final Button[] moveButtons;
    private final Button newMoveButton;

    public LearnMoveDeleteState() {
        newMoveButton = BattleView.createSubMenuButton(2, 1, null);

        // Move Buttons
        moveButtons = new Button[Move.MAX_MOVES];
        for (int i = 0; i < Move.MAX_MOVES; i++) {
            moveButtons[i] = BattleView.createMoveButton(i);
        }
    }

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);

        List<Move> moves = view.getLearnedPokemon().getActualMoves();
        for (int y = 0, i = 0; y < 2; y++) {
            for (int x = 0; x < Move.MAX_MOVES/2 && i < moves.size(); x++, i++) {
                int dx = 22 + x*190, dy = 440 + 21 + y*62;
                g.translate(dx, dy);

                Move move = moves.get(i);
                g.setColor(move.getAttack().getActualType().getColor());
                g.fillRect(0, 0, 183, 55);
                g.drawImage(tiles.getTile(0x22), 0, 0, null);

                g.setColor(Color.BLACK);
                FontMetrics.setFont(g, 22);
                g.drawString(move.getAttack().getName(), 10, 26);

                FontMetrics.setFont(g, 18);
                DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

                BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
                g.drawImage(categoryImage, 12, 32, null);

                g.translate(-dx, -dy);
            }
        }

        g.translate(newMoveButton.x, newMoveButton.y);
        Move move = view.getLearnedMove();
        Color boxColor = move.getAttack().getActualType().getColor();
        g.setColor(boxColor);
        g.fillRect(0, 0, 183, 55);
        g.drawImage(tiles.getTile(0x22), 0, 0, null);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 22);
        g.drawString(move.getAttack().getName(), 10, 26);

        FontMetrics.setFont(g, 18);
        DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

        BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
        g.drawImage(categoryImage, 12, 32, null);

        g.translate(-newMoveButton.x, -newMoveButton.y);

        String msgLine = "Select a move to delete!";

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 25);
        g.drawString(msgLine, newMoveButton.x, 485);

        for (int i = 0; i < moves.size(); i++) {
            moveButtons[i].draw(g);
        }

        newMoveButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        view.setSelectedButton(moveButtons);
        newMoveButton.update();

        for (int i = 0; i < moveButtons.length; i++) {
            if (moveButtons[i].checkConsumePress()) {
                view.getLearnedPokemon().addMove(view.getCurrentBattle(), view.getLearnedMove(), i);

                // This is all done really silly, so we need to do this
                MessageUpdate message = Messages.getNextMessage();
                for (int j = 0; j < Move.MAX_MOVES; j++) {
                    if (j == i) {
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

        if (newMoveButton.checkConsumePress()) {
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
}
