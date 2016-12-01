package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class LearnMoveDeleteState implements VisualStateHandler {

    private final Button[] moveButtons;
    private final Button newMoveButton;

    // Which Pokemon is trying to learn a new move, and which move
    private ActivePokemon learnedPokemon;
    private Move learnedMove;

    public LearnMoveDeleteState() {
//        newMoveButton = new Button(moveButtons[3].x + moveButtons[3].width + moveButtons[2].x, moveButtons[3].y, moveButtons[3].width, moveButtons[3].height, Button.HoverAction.BOX);
        newMoveButton = new Button(0, 0, 0, 0, Button.HoverAction.BOX); // TODO

        // Move Buttons
        moveButtons = new Button[Move.MAX_MOVES];
        for (int y = 0, i = 0; y < 2; y++) {
            for (int x = 0; x < Move.MAX_MOVES/2; x++, i++) {
                moveButtons[i] = new Button(
                        22 + x*190,
                        440 + 21 + y*62,
                        183,
                        55,
                        Button.HoverAction.BOX,
                        new int[] { (i + 1)%Move.MAX_MOVES, // Right
                                ((i - Move.MAX_MOVES/2) + Move.MAX_MOVES)%Move.MAX_MOVES, // Up
                                ((i - 1) + Move.MAX_MOVES)%Move.MAX_MOVES, // Left
                                (i + Move.MAX_MOVES/2)%Move.MAX_MOVES }); // Down
            }
        }
    }

    @Override
    public void checkMessage(MessageUpdate newMessage) {
        if (newMessage.learnMove()) {
            learnedMove = newMessage.getMove();
            learnedPokemon = newMessage.getActivePokemon();
        }
    }

    @Override
    public void reset() {
        learnedMove = null;
        learnedPokemon = null;
    }

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);

        List<Move> moves = learnedPokemon.getActualMoves();
        for (int y = 0, i = 0; y < 2; y++) {
            for (int x = 0; x < Move.MAX_MOVES/2 && i < moves.size(); x++, i++) {
                int dx = 22 + x*190, dy = 440 + 21 + y*62;
                g.translate(dx, dy);

                Move move = moves.get(i);
                g.setColor(move.getAttack().getActualType().getColor());
                g.fillRect(0, 0, 183, 55);
                g.drawImage(tiles.getTile(0x22), 0, 0, null);

                g.setColor(Color.BLACK);
                DrawUtils.setFont(g, 22);
                g.drawString(move.getAttack().getName(), 10, 26);

                DrawUtils.setFont(g, 18);
                DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

                BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
                g.drawImage(categoryImage, 12, 32, null);

                g.translate(-dx, -dy);
            }
        }

        g.translate(newMoveButton.x, newMoveButton.y);
        Move move = learnedMove;
        Color boxColor = move.getAttack().getActualType().getColor();
        g.setColor(boxColor);
        g.fillRect(0, 0, 183, 55);
        g.drawImage(tiles.getTile(0x22), 0, 0, null);

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 22);
        g.drawString(move.getAttack().getName(), 10, 26);

        DrawUtils.setFont(g, 18);
        DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

        BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
        g.drawImage(categoryImage, 12, 32, null);

        g.translate(-newMoveButton.x, -newMoveButton.y);

        String msgLine = "Select a move to delete!";

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 25);
        g.drawString(msgLine, newMoveButton.x, 485);

        for (int i = 0; i < moves.size(); i++) {
            moveButtons[i].draw(g);
        }

        newMoveButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        view.selectedButton = Button.update(moveButtons, view.selectedButton);
        newMoveButton.update();

        for (int i = 0; i < moveButtons.length; i++) {
            if (moveButtons[i].checkConsumePress()) {
                learnedPokemon.addMove(view.getCurrentBattle(), learnedMove, i);

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

    public Move getLearnedMove() {
        return this.learnedMove;
    }
}
