package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.View;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import pokemon.ActivePokemon;
import trainer.Trainer.Action;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class FightState implements VisualStateHandler {

    @Override
    public void set(BattleView view) {
        view.selectedButton = view.lastMoveUsed;
        view.selectedMoveList = view.currentBattle.getPlayer().front().getMoves(view.currentBattle);
        for (int i = 0; i < Move.MAX_MOVES; i++) {
            view.moveButtons[i].setActive(i < view.selectedMoveList.size());
        }

        for (Button b: view.moveButtons) {
            b.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x20), 415, 440, null);
        g.drawImage(tiles.getTile(0x21), 0, 440, null);

        ActivePokemon playerPokemon = view.currentBattle.getPlayer().front();

        List<Move> moves = playerPokemon.getMoves(view.currentBattle);
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

        // TODO: See if I can use isNullOrEmpty
        String msgLine = view.state == VisualState.INVALID_FIGHT && view.message != null ? view.message : "Select a move!";

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 30);
        DrawUtils.drawWrappedText(g, msgLine, 440, 485, 350); // TODO: Is this duplicated code?

        View.drawArrows(g, null, view.backButton);

        for (int i = 0; i < Move.MAX_MOVES && i < moves.size(); i++) {
            view.moveButtons[i].draw(g);
        }

        view.backButton.draw(g);
    }

    @Override
    public void update(BattleView view) {
        // Update move buttons and the back button
        view.selectedButton = Button.update(view.moveButtons, view.selectedButton);
        view.backButton.update(false, ControlKey.BACK);

        // Get the Pokemon that is attacking and their corresponsing move list
        ActivePokemon front = view.currentBattle.getPlayer().front();

        for (int i = 0; i < view.selectedMoveList.size(); i++) {
            if (view.moveButtons[i].checkConsumePress()) {
                view.lastMoveUsed = i;

                // Execute the move if valid
                if (Move.validMove(view.currentBattle, front, view.selectedMoveList.get(i), true)) {
                    view.currentBattle.getPlayer().performAction(view.currentBattle, Action.FIGHT);
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
        if (view.backButton.checkConsumePress()) {
            view.setVisualState(VisualState.MENU);
        }
    }
}
