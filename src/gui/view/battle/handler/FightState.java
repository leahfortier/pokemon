package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import gui.Button;
import gui.MessagePanel;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer.Action;
import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class FightState implements VisualStateHandler {

    private final Button[] moveButtons;

    private List<Move> selectedMoveList;

    // The last move that a Pokemon used
    private int lastMoveUsed;

    public FightState() {

        // Move Buttons
        moveButtons = new Button[Move.MAX_MOVES];
        for (int i = 0; i < Move.MAX_MOVES; i++) {
            moveButtons[i] = BattleView.createMoveButton(i);
        }
    }

    @Override
    public void reset() {
        this.resetLastMoveUsed();
    }

    @Override
    public void set(BattleView view) {
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
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        MessagePanel messagePanel = new MessagePanel(415, 440, 385, 161, new Color(53, 53, 129));
        messagePanel.drawBackground(g);

        g.drawImage(tiles.getTile(0x21), 0, 440, null);

        ActivePokemon playerPokemon = Game.getPlayer().front();

        List<Move> moves = playerPokemon.getMoves(view.getCurrentBattle());
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

        String msgLine = view.getMessage(VisualState.INVALID_FIGHT, "Select a move!");
        messagePanel.drawText(g, 30, msgLine);

        view.drawBackButton(g);

        for (int i = 0; i < Move.MAX_MOVES && i < moves.size(); i++) {
            moveButtons[i].draw(g);
        }
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
