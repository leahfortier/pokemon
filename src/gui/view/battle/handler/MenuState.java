package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import gui.Button;
import gui.ButtonHoverAction;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import main.Game;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.Trainer.Action;
import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class MenuState implements VisualStateHandler {

    // Menu Button Indexes
    private static final int FIGHT_BUTTON = 0;
    private static final int BAG_BUTTON = 1;
    private static final int SWITCH_BUTTON = 2;
    private static final int RUN_BUTTON = 3;

    private final Button fightBtn;
    private final Button bagBtn;
    private final Button pokemonBtn;
    private final Button runBtn;

    private final Button[] menuButtons;

    public MenuState() {
        // Menu Buttons
        menuButtons = new Button[4]; // TODO: ugly equals
        menuButtons[FIGHT_BUTTON] = fightBtn = new Button(
                452,
                473,
                609 - 452,
                515 - 473,
                ButtonHoverAction.ARROW,
                new int[] {	BAG_BUTTON, SWITCH_BUTTON, RUN_BUTTON, SWITCH_BUTTON }
        );
        menuButtons[BAG_BUTTON] = bagBtn = new Button(
                628,
                473,
                724 - 628,
                513 - 473,
                ButtonHoverAction.ARROW,
                new int[] { SWITCH_BUTTON, RUN_BUTTON, FIGHT_BUTTON, RUN_BUTTON }
        );
        menuButtons[SWITCH_BUTTON] = pokemonBtn = new Button(
                452,
                525,
                609 - 452,
                571 - 525,
                ButtonHoverAction.ARROW,
                new int[] { RUN_BUTTON, FIGHT_BUTTON, BAG_BUTTON, FIGHT_BUTTON }
        );
        menuButtons[RUN_BUTTON] = runBtn = new Button(
                628,
                525,
                724 - 628,
                571 - 525,
                ButtonHoverAction.ARROW,
                new int[] { FIGHT_BUTTON, BAG_BUTTON, SWITCH_BUTTON, BAG_BUTTON }
        );
    }

    @Override
    public void set(BattleView view) {
        for (Button button: menuButtons) {
            button.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);
        g.drawImage(tiles.getTile(0x2), 0, 0, null);

        g.setColor(Color.BLACK);

        ActivePokemon playerPokemon = Game.getPlayer().front();

        FontMetrics.setFont(g, 30);
        DrawUtils.drawWrappedText(g, "What will " + playerPokemon.getActualName() + " do?", 20, 485, 400);

        for (Button button: menuButtons) {
            button.draw(g);
        }
    }

    @Override
    public void update(BattleView view) {
        Battle currentBattle = view.getCurrentBattle();
        CharacterData player = Game.getPlayer();

        // Update menu buttons
        view.setSelectedButton(menuButtons);

        // Show Bag View
        if (bagBtn.checkConsumePress()) {
            view.setVisualState(VisualState.BAG);
        }
        // Show Pokemon View
        else if (pokemonBtn.checkConsumePress()) {
            view.setVisualState(VisualState.POKEMON);
        }
        // Attempt escape
        else if (runBtn.checkConsumePress()) {
            view.setVisualState(VisualState.MESSAGE);
            currentBattle.runAway();
            view.cycleMessage(false);
        }
        // Show Fight View TODO: Semi-invulnerable moves look awful and weird
        else if (fightBtn.checkConsumePress() || player.front().isSemiInvulnerable()) {
            view.setVisualState(VisualState.FIGHT);

            // Move is forced -- don't show menu, but execute the move
            if (Move.forceMove(currentBattle, player.front())) {
                player.performAction(currentBattle, Action.FIGHT);
                view.setVisualState(VisualState.MESSAGE);
                view.cycleMessage(false);
            }
        }
        else if (InputControl.instance().consumeIfDown(ControlKey.LOG)) {
            view.setVisualState(VisualState.LOG_VIEW);
        }
    }
}
