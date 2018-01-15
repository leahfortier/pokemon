package gui.view.battle.handler;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import draw.button.Button;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import main.Game;
import trainer.TrainerAction;
import trainer.player.Player;

import java.awt.Color;
import java.awt.Graphics;

public class MenuState implements VisualStateHandler {

    private Button[] menuButtons;

    @Override
    public void set(BattleView view) {
        this.menuButtons = view.createPanelButtons();

        for (Button button : menuButtons) {
            button.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        ActivePokemon playerPokemon = Game.getPlayer().front();
        view.drawMenuMessagePanel(g, "What will " + playerPokemon.getActualName() + " do?");
        view.drawButtonsPanel(g);

        for (MenuChoice menuChoice : MenuChoice.values()) {
            Button menuButton = getButton(menuChoice);

            menuButton.fillBordered(g, menuChoice.buttonColor);
            menuButton.label(g, 30, menuChoice.getButtonLabel());
        }

        for (Button button : menuButtons) {
            button.draw(g);
        }
    }

    @Override
    public void update(BattleView view) {
        Battle currentBattle = view.getCurrentBattle();
        Player player = Game.getPlayer();

        // Update menu buttons
        view.setSelectedButton(menuButtons);

        // Show Bag View
        if (getButton(MenuChoice.BAG).checkConsumePress()) {
            view.setVisualState(VisualState.BAG);
        }
        // Show Pokemon View
        else if (getButton(MenuChoice.SWITCH).checkConsumePress()) {
            view.setVisualState(VisualState.POKEMON);
        }
        // Attempt escape
        else if (getButton(MenuChoice.RUN).checkConsumePress()) {
            view.setVisualState(VisualState.MESSAGE);
            currentBattle.runAway();
            view.cycleMessage(false);
        }
        // Show Fight View TODO: Semi-invulnerable moves look awful and weird
        else if (getButton(MenuChoice.FIGHT).checkConsumePress() || player.front().isSemiInvulnerable()) {
            view.setVisualState(VisualState.FIGHT);

            // Move is forced -- don't show menu, but execute the move
            if (Move.forceMove(currentBattle, player.front())) {
                player.performAction(currentBattle, TrainerAction.FIGHT);
                view.setVisualState(VisualState.MESSAGE);
                view.cycleMessage(false);
            }
        } else if (InputControl.instance().consumeIfDown(ControlKey.LOG)) {
            view.setVisualState(VisualState.LOG_VIEW);
        }
    }

    private Button getButton(MenuChoice menuChoice) {
        return this.menuButtons[menuChoice.ordinal()];
    }

    private enum MenuChoice {
        FIGHT(new Color(220, 20, 20)),
        SWITCH(new Color(35, 120, 220)),
        BAG(new Color(120, 200, 80)),
        RUN(new Color(255, 215, 0));

        private final Color buttonColor;

        MenuChoice(Color buttonColor) {
            this.buttonColor = buttonColor;
        }

        public String getButtonLabel() {
            return this.name();
        }
    }
}
