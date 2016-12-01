package gui.view.battle;

import gui.TileSet;
import gui.view.battle.handler.BagState;
import gui.view.battle.handler.FightState;
import gui.view.battle.handler.LearnMoveDeleteState;
import gui.view.battle.handler.LearnMoveQuestionState;
import gui.view.battle.handler.LogState;
import gui.view.battle.handler.MenuState;
import gui.view.battle.handler.MessageState;
import gui.view.battle.handler.PokemonState;
import gui.view.battle.handler.VisualStateHandler;

import java.awt.Graphics;

// Contains the different types of states a battle can be in
public enum VisualState {
    MESSAGE(new MessageState()),
    BAG(new BagState()),
    INVALID_BAG(BAG.visualStateHandler),
    FIGHT(new FightState()),
    INVALID_FIGHT(FIGHT.visualStateHandler),
    POKEMON(new PokemonState()),
    INVALID_POKEMON(POKEMON.visualStateHandler),
    MENU(new MenuState()),
    LEARN_MOVE_QUESTION(new LearnMoveQuestionState()),
    LEARN_MOVE_DELETE(new LearnMoveDeleteState()),
    USE_ITEM(POKEMON.visualStateHandler),
    STAT_GAIN(MESSAGE.visualStateHandler),
    LOG_VIEW(new LogState());

    private final VisualStateHandler visualStateHandler;

    VisualState(VisualStateHandler visualStateHandler) {
        this.visualStateHandler = visualStateHandler;
    }

    public void update(BattleView battleView) {
        this.visualStateHandler.update(battleView);
    }

    public void set(BattleView battleView) {
        this.visualStateHandler.set(battleView);
    }

    public void draw(BattleView battleView, Graphics g, TileSet tiles) {
        this.visualStateHandler.draw(battleView, g, tiles);
    }
}
