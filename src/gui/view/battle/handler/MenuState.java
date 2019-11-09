package gui.view.battle.handler;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import draw.button.ButtonList;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import main.Game;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import trainer.player.Player;

import java.awt.Color;
import java.awt.Graphics;

public class MenuState implements VisualStateHandler {
    private final ButtonList menuButtons;

    private BattleView view;
    private Battle currentBattle;

    public MenuState() {
        view = Game.instance().getBattleView();

        MenuChoice[] choices = MenuChoice.values();
        menuButtons = new ButtonList(
                view.createPanelLayout(MenuChoice.values().length)
                    .withDrawSetup((panel, index) -> panel.withTransparentBackground(choices[index].buttonColor)
                                                          .withTransparentCount(2)
                                                          .withBorderPercentage(15)
                                                          .withBlackOutline()
                                                          .withLabel(choices[index].getButtonLabel(), 30))
                    .withPressIndex(index -> choices[index].pressAction.press(this))
                    .getButtons()
        );
    }

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
        menuButtons.update();

        // Semi-invulnerable moves don't get a choice -- gotta fight
        if (Game.getPlayer().front().isSemiInvulnerable()) {
            pressFight();
        } else if (InputControl.instance().consumeIfDown(ControlKey.LOG)) {
            view.setVisualState(VisualState.LOG_VIEW);
        } else if (menuButtons.consumeSelectedPress()) {
            view.setVisualState();
        }
    }

    @Override
    public ButtonList getButtons() {
        return menuButtons;
    }

    @Override
    public void reset(BattleView view) {
        this.view = view;
        this.currentBattle = view.getCurrentBattle();
    }

    private void pressFight() {
        Player player = Game.getPlayer();

        // If move is forced -- don't show menu, just execute the move
        if (Move.forceMove(currentBattle, player.front())) {
            view.executeMove();
        } else {
            // Otherwise, select a move from the fight menu
            view.setVisualState(VisualState.FIGHT);
        }
    }

    private void pressRun() {
        if (currentBattle.runAway()) {
            Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
        }
        view.setVisualState(VisualState.MESSAGE);
        view.cycleMessage();
    }

    private enum MenuChoice {
        FIGHT(new Color(220, 20, 20), MenuState::pressFight),
        SWITCH(new Color(35, 120, 220), VisualState.POKEMON),
        BAG(new Color(120, 200, 80), VisualState.BAG),
        RUN(new Color(255, 215, 0), MenuState::pressRun);

        private final Color buttonColor;
        private final MenuPressAction pressAction;

        MenuChoice(Color buttonColor, VisualState visualState) {
            this(buttonColor, state -> state.view.setVisualState(visualState));
        }

        MenuChoice(Color buttonColor, MenuPressAction pressAction) {
            this.buttonColor = buttonColor;
            this.pressAction = pressAction;
        }

        public String getButtonLabel() {
            return this.name();
        }

        @FunctionalInterface
        private interface MenuPressAction {
            void press(MenuState state);
        }
    }
}
