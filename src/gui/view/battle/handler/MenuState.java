package gui.view.battle.handler;

import battle.attack.Move;
import gui.Button;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import pokemon.ActivePokemon;
import trainer.Trainer.Action;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public class MenuState implements VisualStateHandler {

    @Override
    public void set(BattleView view) {
        for (Button b: view.menuButtons) {
            b.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);
        g.drawImage(tiles.getTile(0x2), 0, 0, null);

        g.setColor(Color.BLACK);

        ActivePokemon playerPokemon = view.currentBattle.getPlayer().front();

        DrawUtils.setFont(g, 30);
        DrawUtils.drawWrappedText(g, "What will " + playerPokemon.getActualName() + " do?", 20, 485, 400);

        for (Button b: view.menuButtons) {
            b.draw(g);
        }
    }

    @Override
    public void update(BattleView view) {
        // Update menu buttons
        view.selectedButton = Button.update(view.menuButtons, view.selectedButton);

        // Show Bag View
        if (view.bagBtn.checkConsumePress()) {
            view.setVisualState(VisualState.BAG);
        }
        // Show Pokemon View
        else if (view.pokemonBtn.checkConsumePress()) {
            view.setVisualState(VisualState.POKEMON);
        }
        // Attempt escape
        else if (view.runBtn.checkConsumePress()) {
            view.setVisualState(VisualState.MESSAGE);
            view.currentBattle.runAway();
            view.cycleMessage(false);
        }
        // Show Fight View TODO: Semi-invulnerable moves look awful and weird
        else if (view.fightBtn.checkConsumePress() || view.currentBattle.getPlayer().front().isSemiInvulnerable()) {
            view.setVisualState(VisualState.FIGHT);

            // Move is forced -- don't show menu, but execute the move
            if (Move.forceMove(view.currentBattle, view.currentBattle.getPlayer().front())) {
                view.currentBattle.getPlayer().performAction(view.currentBattle, Action.FIGHT);
                view.setVisualState(VisualState.MESSAGE);
                view.cycleMessage(false);
            }
        }
        else if (InputControl.instance().consumeIfDown(ControlKey.L)) {
            view.logPage = 0;
            view.logMessages = view.currentBattle.getPlayer().getLogMessages();

            if (view.logMessages.size() / BattleView.LOGS_PER_PAGE > 0) {
                view.selectedButton = BattleView.LOG_RIGHT_BUTTON;
                view.logRightButton.setActive(true);
                view.selectedButton = Button.update(view.logButtons, view.selectedButton);
            }
            else {
                view.logRightButton.setActive(false);
            }

            view.logLeftButton.setActive(false);
            view.setVisualState(VisualState.LOG_VIEW);
        }
    }
}
