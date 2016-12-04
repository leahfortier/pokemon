package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import gui.Button;
import gui.panel.DrawPanel;
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
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        String message = view.getMessage(VisualState.INVALID_FIGHT, "Select a move!");
        view.drawMessagePanel(g, message);
        view.drawButtonsPanel(g);

        ActivePokemon playerPokemon = Game.getPlayer().front();

        List<Move> moves = playerPokemon.getMoves(view.getCurrentBattle());
        for (int i = 0; i < moves.size(); i++) {
            int dx = this.moveButtons[i].x;
            int dy = this.moveButtons[i].y;

            g.translate(dx, dy);

            Move move = moves.get(i);
            DrawPanel movePanel = new DrawPanel(0, 0, 183, 55)
                    .withBackgroundColor(move.getAttack().getActualType().getColor())
                    .withDarkBorder()
                    .withBlackOutline();
            movePanel.drawBackground(g);

            g.setColor(Color.BLACK);
            FontMetrics.setFont(g, 22);
            g.drawString(move.getAttack().getName(), 10, 26);

            FontMetrics.setFont(g, 18);
            DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 170, 45);

            BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
            g.drawImage(categoryImage, 12, 32, null);

            g.translate(-dx, -dy);

            moveButtons[i].draw(g);
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
