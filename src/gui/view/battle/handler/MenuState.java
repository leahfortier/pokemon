package gui.view.battle.handler;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import draw.button.Button;
import draw.button.ButtonList;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import main.Game;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import trainer.TrainerAction;
import trainer.player.Player;

import java.awt.Color;
import java.awt.Graphics;

public class MenuState implements VisualStateHandler {
    private ButtonList menuButtons;

    private BattleView view;

    @Override
    public void set() {
        menuButtons.setFalseHover();
    }

    @Override
    public void draw(Graphics g) {
        ActivePokemon playerPokemon = Game.getPlayer().front();
        view.drawMenuMessagePanel(g, "What will " + playerPokemon.getActualName() + " do?");
        view.drawButtonsPanel(g);

        menuButtons.draw(g);
    }

    @Override
    public void update() {
        Battle currentBattle = view.getCurrentBattle();
        Player player = Game.getPlayer();

        // Update menu buttons
        menuButtons.update();

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
            if (currentBattle.runAway()) {
                Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
            }
            view.cycleMessage();
        }
        // Show Fight View TODO: Semi-invulnerable moves look awful and weird
        else if (getButton(MenuChoice.FIGHT).checkConsumePress() || player.front().isSemiInvulnerable()) {
            view.setVisualState(VisualState.FIGHT);

            // Move is forced -- don't show menu, but execute the move
            if (Move.forceMove(currentBattle, player.front())) {
                player.performAction(currentBattle, TrainerAction.FIGHT);
                view.setVisualState(VisualState.MESSAGE);
                view.cycleMessage();
            }
        } else if (InputControl.instance().consumeIfDown(ControlKey.LOG)) {
            view.setVisualState(VisualState.LOG_VIEW);
        }
    }

    private Button getButton(MenuChoice menuChoice) {
        return this.menuButtons.get(menuChoice.ordinal());
    }

    @Override
    public ButtonList getButtons() {
        return menuButtons;
    }

    @Override
    public void reset(BattleView view) {
        this.view = view;

        MenuChoice[] choices = MenuChoice.values();
        menuButtons = new ButtonList(
                view.createPanelLayout(MenuChoice.values().length)
                    .withDrawSetup((panel, index) -> panel.withTransparentBackground(choices[index].buttonColor)
                                                          .withTransparentCount(2)
                                                          .withBorderPercentage(15)
                                                          .withBlackOutline()
                                                          .withLabel(choices[index].getButtonLabel(), 30))
                    .getButtons()
        );
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
